<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import { page } from '$app/stores';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
    import ConfirmationModal from '$lib/components/ConfirmationModal.svelte';
	import { fetchApi, fetchJson, ApiError } from '$lib/api';
    import { toast } from '$lib/stores/toast.svelte';

	let groupId = $derived($page.params.id);
	let group = $state<any>(null);
	let members = $state<any[]>([]);
	let schedules = $state<any[]>([]);
	let currentUser = $state<any>(null);
	
	let isLoading = $state(true);
	let error = $state('');

	let isManager = $derived(
		members.find(m => m.id === currentUser?.id)?.role === 'MANAGER'
	);

	let openHeatmapId = $state<string | null>(null);
	let expandedPartyId = $state<string | null>(null);
    let loadingPartyId = $state<string | null>(null);
	let parties = $state<any[]>([]);
	let heatmapData = $state<any>(null); // [dayIndex][hourIndex] = count
    let rawAvailability = $state<any[]>([]);
    let selectedCell = $state<{d: number, t: number, members: string[]} | null>(null);

    let deleteScheduleModal: ReturnType<typeof ConfirmationModal>;
    let scheduleToDelete = $state<string | null>(null);

    let deletePartyModal: ReturnType<typeof ConfirmationModal>;
    let partyToDelete = $state<any | null>(null);

    let kickMemberModal: ReturnType<typeof ConfirmationModal>;
    let memberToKick = $state<any | null>(null);

	async function fetchData() {
	        try {
	                const [groupData, memberData, scheduleData, userData] = await Promise.all([
	                        fetchJson<any>(`/api/groups/${groupId}`),
	                        fetchJson<any[]>(`/api/groups/${groupId}/members`),
	                        fetchJson<any[]>(`/api/groups/${groupId}/schedules`),
	                        fetchJson<any>('/api/users/me')
	                ]);

	                group = groupData;
	                members = memberData;
	                schedules = scheduleData;
	                currentUser = userData;

	                // Eagerly fetch parties for all discovered schedules
	                if (schedules.length > 0) {
	                    await Promise.all(schedules.map(s => fetchParties(s.id)));
	                }
	        } catch (err) {			console.error(err);
			if (!(err instanceof ApiError && err.status === 401)) {
				error = 'Failed to load group details.';
			}
		} finally {
			isLoading = false;
		}
	}

	async function fetchParties(scheduleId: string) {
		try {
			const data = await fetchJson<any[]>(`/api/schedules/${scheduleId}/parties`);
			parties = [...parties.filter(p => p.scheduleId !== scheduleId), ...data.map(p => ({ ...p, scheduleId }))];
		} catch (err) {
			console.error('Failed to fetch parties:', err);
		}
	}

    async function fetchHeatmap(scheduleId: string, startDate: string) {
        try {
            const data = await fetchJson<any[]>(`/api/schedules/${scheduleId}/availability`);
            rawAvailability = data;
            
            // Initialize 8x24 grid with zeros
            const grid = Array(8).fill(0).map(() => Array(24).fill(0));
            
            const scheduleStartAbs = new Date(startDate);
            const baseDate = new Date(scheduleStartAbs);
            baseDate.setHours(0, 0, 0, 0);

            data.forEach(userAvail => {
                userAvail.blocks.forEach((block: any) => {
                    const blockDate = new Date(block.start);
                    const diffMs = blockDate.getTime() - baseDate.getTime();
                    const diffHoursTotal = Math.floor(diffMs / (1000 * 60 * 60));
                    const dayDiff = Math.floor(diffHoursTotal / 24);
                    const hour = diffHoursTotal % 24;

                    if (dayDiff >= 0 && dayDiff < 8 && hour >= 0 && hour < 24) {
                        grid[dayDiff][hour]++;
                    }
                });
            });

            heatmapData = grid;
        } catch (err) {
            console.error('Failed to fetch heatmap:', err);
        }
    }

	async function toggleHeatmap(id: string, startDate: string) {
		if (openHeatmapId !== id) {
            await fetchHeatmap(id, startDate);
        }
        openHeatmapId = openHeatmapId === id ? null : id;
        selectedCell = null;
	}

    async function renameSchedule(scheduleId: string, oldTitle: string) {
        const schedule = schedules.find(s => s.id === scheduleId);
        const newTitle = prompt(`Enter new title for "${oldTitle}":`, oldTitle);
        if (newTitle && newTitle !== oldTitle) {
            try {
                await fetchApi(`/api/groups/schedules/${scheduleId}`, {
                    method: 'PUT',
                    body: JSON.stringify({
                        title: newTitle,
                        startTime: schedule.start,
                        endTime: schedule.end
                    })
                });
                await fetchData();
                toast.success('Schedule renamed.');
            } catch (err) {
                console.error(err);
                toast.error('Failed to rename schedule.');
            }
        }
    }

    async function requestDeleteSchedule(scheduleId: string) {
        scheduleToDelete = scheduleId;
        deleteScheduleModal.show();
    }

    async function confirmDeleteSchedule() {
        if (!scheduleToDelete) return;
        try {
            await fetchApi(`/api/groups/schedules/${scheduleToDelete}`, { method: 'DELETE' });
            await fetchData();
            toast.success('Schedule deleted.');
        } catch (err) {
            console.error(err);
            toast.error('Failed to delete schedule.');
        } finally {
            scheduleToDelete = null;
        }
    }

    async function requestDeleteParty(party: any) {
        partyToDelete = party;
        deletePartyModal.show();
    }

    async function confirmDeleteParty() {
        if (!partyToDelete) return;
        try {
            await fetchApi(`/api/parties/${partyToDelete.id}`, { method: 'DELETE' });
            await fetchParties(partyToDelete.scheduleId);
            toast.success('Party deleted.');
        } catch (err) {
            console.error(err);
            toast.error('Failed to delete party.');
        } finally {
            partyToDelete = null;
        }
    }

    async function togglePartyStatus(party: any) {
        try {
            const newStatus = party.status !== 'Done';
            await fetchApi(`/api/parties/${party.id}/complete`, { 
                method: 'PATCH',
                body: JSON.stringify({ completed: newStatus })
            });
            await fetchParties(party.scheduleId);
            toast.success(newStatus ? 'Party marked as Done.' : 'Party marked as Planned.');
        } catch (err) {
            console.error(err);
            toast.error('Failed to update party status.');
        }
    }

    async function requestKickMember(member: any) {
        memberToKick = member;
        kickMemberModal.show();
    }

    async function confirmKickMember() {
        if (!memberToKick) return;
        try {
            await fetchApi(`/api/groups/${groupId}/members/${memberToKick.id}`, { method: 'DELETE' });
            await fetchData();
            toast.success(`${memberToKick.username} has been removed from the group.`);
        } catch (err) {
            console.error(err);
            toast.error('Failed to remove member.');
        } finally {
            memberToKick = null;
        }
    }

    async function toggleAuditorRole(member: any) {
        try {
            const isCurrentlyAuditor = member.role === 'AUDITOR';
            const newRole = isCurrentlyAuditor ? 'MEMBER' : 'AUDITOR';
            
            await fetchApi(`/api/groups/${groupId}/members/${member.id}/role`, {
                method: 'PATCH',
                body: JSON.stringify({ role: newRole })
            });
            await fetchData();
            toast.success(`Role updated to ${newRole} for ${member.username}.`);
        } catch (err) {
            console.error(err);
            toast.error('Failed to update role. (Backend implementation planned)');
        }
    }

    function notifyMember(member: any) {
        toast.info(`Notification feature for ${member.username} is planned.`);
    }

    function formatDateOffset(dateString: string, offsetDays: number) {
		const d = new Date(dateString);
		d.setDate(d.getDate() + offsetDays);
		const day = String(d.getDate()).padStart(2, '0');
        const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
        const weekday = weekdays[d.getDay()];
		return `${day} (${weekday})`;
	}

    function getFullDate(dateString: string, offsetDays: number) {
        const d = new Date(dateString);
		d.setDate(d.getDate() + offsetDays);
        return d.toLocaleDateString();
    }

    function getHeatmapClass(count: number) {
        if (!count) return 'bg-base-100';
        const total = Math.max(members.length, 1);
        const ratio = count / total;
        if (ratio >= 0.8) return 'bg-success text-success-content';
        if (ratio >= 0.5) return 'bg-primary text-primary-content';
        if (ratio >= 0.2) return 'bg-info text-info-content';
        return 'bg-warning text-warning-content';
    }

    function handleCellClick(d: number, t: number, scheduleStart: string) {
        if (!scheduleStart) return;
        const baseDate = new Date(scheduleStart);
        baseDate.setHours(0, 0, 0, 0);
        const targetTime = baseDate.getTime() + (d * 24 + t) * 60 * 60 * 1000;

        const availMembers = rawAvailability
            .filter(userAvail => 
                userAvail.blocks.some((b: any) => new Date(b.start).getTime() === targetTime)
            )
            .map(ua => {
                const m = members.find(m => m.id === ua.userId);
                return m ? (m.nickname || m.username) : 'Unknown';
            });

        selectedCell = { d, t, members: availMembers };
    }

	async function toggleParty(id: string, scheduleId: string) {
		if (expandedPartyId !== id) {
            loadingPartyId = id;
			await fetchParties(scheduleId);
            loadingPartyId = null;
		}
		expandedPartyId = expandedPartyId === id ? null : id;
	}

	async function joinParty(partyId: string, scheduleId: string) {
		try {
			await fetchApi(`/api/parties/${partyId}/join`, { method: 'POST' });
			await fetchParties(scheduleId);
            toast.success('Joined the party!');
		} catch (err) {
			console.error(err);
            await fetchParties(scheduleId);
            toast.error('Failed to join. Syncing latest data...');
		}
	}

	async function leaveParty(partyId: string, scheduleId: string) {
		try {
			await fetchApi(`/api/parties/${partyId}/leave`, { method: 'POST' });
			await fetchParties(scheduleId);
            toast.success('Left the party.');
		} catch (err) {
			console.error(err);
            await fetchParties(scheduleId);
            toast.error('Failed to leave. Syncing latest data...');
		}
	}

	function logout() {
		localStorage.removeItem('ark_token');
		window.location.href = `${base}/`;
	}

	onMount(async () => {
		if (!localStorage.getItem('ark_token')) {
			window.location.href = `${base}/login`;
			return;
		}
		await fetchData();
	});
</script>

<svelte:head>
	<title>{group ? group.name : 'Loading...'} - Ark Resolver</title>
</svelte:head>

<div class="min-h-screen bg-base-200 flex flex-col font-sans">
	<div class="navbar bg-base-100 shadow-sm sticky top-0 z-50">
		<div class="navbar-start">
			<a class="btn btn-ghost text-xl font-bold" href="{base}/dashboard">Ark Resolver</a>
		</div>
		<div class="navbar-end gap-2">
			<ThemeToggle />
			<button class="btn btn-outline btn-error btn-sm md:btn-md" onclick={logout}>Logout</button>
		</div>
	</div>

	<main class="flex-1 p-4 md:p-8">
		<div class="container mx-auto max-w-6xl">
			<div class="flex items-center gap-4 mb-8">
				<a href="{base}/dashboard" class="btn btn-ghost btn-circle" aria-label="Back to Dashboard">
					<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" /></svg>
				</a>
				<h1 class="text-3xl font-bold text-ubuntu">Group Details</h1>
			</div>

			{#if isLoading}
				<div class="flex justify-center items-center h-64">
					<span class="loading loading-spinner loading-lg text-primary"></span>
				</div>
			{:else if error}
				<div class="alert alert-error shadow-lg">
					<span>{error}</span>
					<a href="{base}/dashboard" class="btn btn-sm btn-ghost">Return to Dashboard</a>
				</div>
			{:else if group}
				<!-- Top Section: Group Details -->
				<div class="card bg-base-100 shadow-xl mb-8 border-l-4 border-primary">
					<div class="card-body flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
						<div>
							<h2 class="text-4xl font-bold text-ubuntu mb-2">{group.name}</h2>
							{#if group.description}
								<p class="opacity-80 font-neo">{group.description}</p>
							{/if}
						</div>
                        <div class="flex flex-wrap gap-2 w-full md:w-auto">
                            {#if isManager}
                                <a href="{base}/groups/{groupId}/invites" class="btn btn-primary flex-1 md:flex-none">Invite Members</a>
                            {/if}
                            <a href="{base}/groups/{groupId}/settings" class="btn btn-outline text-ubuntu font-bold flex-1 md:flex-none">Settings</a>
                        </div>
					</div>
				</div>

				<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
					<!-- Main Section: Schedules -->
					<div class="lg:col-span-2 space-y-6">
						<div class="flex justify-between items-center">
							<h3 class="text-2xl font-bold text-ubuntu">Schedules</h3>
							{#if isManager}
								<a href="{base}/groups/{groupId}/schedules/create" class="btn btn-sm btn-secondary">New Schedule</a>
							{/if}
						</div>

						{#if schedules.length === 0}
							<div class="card bg-base-100 shadow-md">
								<div class="card-body text-center opacity-60 py-12">
									No schedules have been created yet.
								</div>
							</div>
						{:else}
							{#each schedules as schedule}
								<div class="card bg-base-100 shadow-md hover:shadow-lg transition-shadow border-l-4 {schedule.status === 'ACTIVE' ? 'border-success' : 'border-neutral'}">
									<div class="card-body p-4 sm:p-5">
										<div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
											<div class="w-full">
												<div class="flex justify-between sm:justify-start items-center gap-2">
													<h4 class="text-lg font-bold text-ubuntu">{schedule.title}</h4>
													<div class="flex items-center gap-2">
                                                        <div class="badge {schedule.status === 'ACTIVE' ? 'badge-success text-white' : 'badge-neutral'}">
                                                            {schedule.status}
                                                        </div>
                                                        {#if isManager}
                                                            <div class="dropdown dropdown-end dropdown-bottom">
                                                                <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                                <!-- svelte-ignore a11y_label_has_associated_control -->
                                                                <label tabindex="0" class="btn btn-ghost btn-xs btn-circle opacity-50 hover:opacity-100">
                                                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="M6.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM12.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM18.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" /></svg>
                                                                </label>
                                                                <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                                <ul tabindex="0" class="dropdown-content z-[10] menu p-2 shadow bg-base-100 rounded-box w-32 border border-base-200">
                                                                    <li><button class="text-sm" onclick={() => renameSchedule(schedule.id, schedule.title)}>Rename</button></li>
                                                                    <li><button class="text-sm text-error" onclick={() => requestDeleteSchedule(schedule.id)}>Delete</button></li>
                                                                </ul>
                                                            </div>
                                                        {/if}
                                                    </div>
												</div>
												<p class="text-xs opacity-70 mt-1 font-mono">{new Date(schedule.start).toLocaleString()} ~ {new Date(schedule.end).toLocaleString()}</p>
											</div>
											<div class="flex gap-2 w-full sm:w-auto">
												<a href="{base}/schedules/{schedule.id}/availability" class="btn btn-sm btn-outline flex-1 sm:flex-none">Submit Availability</a>
												<button class="btn btn-sm btn-primary flex-1 sm:flex-none" onclick={() => toggleHeatmap(schedule.id, schedule.start)}>
													{openHeatmapId === schedule.id ? 'Hide Heatmap' : 'View Heatmap'}
												</button>
											</div>
										</div>

										{#if openHeatmapId === schedule.id && heatmapData}
											<div class="mt-4 relative bg-base-200 rounded-lg overflow-hidden border border-base-300">
												<div class="hidden md:block overflow-x-auto p-2">
													<table class="table table-xs w-full text-center border-collapse">
														<thead>
															<tr>
																<th class="border border-base-300 sticky left-0 bg-base-200 w-12 z-10"></th>
																{#each Array(24) as _, i}
																	<th class="font-mono px-0 border border-base-300 text-[10px] w-5">{i}</th>
																{/each}
															</tr>
														</thead>
														<tbody>
															{#each Array(8) as _, d}
																<tr>
																	<th class="font-mono whitespace-nowrap px-1 py-1 border border-base-300 sticky left-0 bg-base-200 text-[10px] z-10" title={getFullDate(schedule.start, d)}>{formatDateOffset(schedule.start, d)}</th>
																	{#each Array(24) as _, t}
																		<td 
                                                                            class="border border-base-300 transition-colors p-0 cursor-pointer {getHeatmapClass(heatmapData[d][t])}" 
                                                                            title="{getFullDate(schedule.start, d)} {t}:00 - Available: {heatmapData[d][t]}"
                                                                            onclick={() => handleCellClick(d, t, schedule.start)}
                                                                        >
																			<div class="w-full h-4 text-[8px] flex items-center justify-center font-bold">
                                                                                {heatmapData[d][t] > 0 ? heatmapData[d][t] : ''}
                                                                            </div>
																		</td>
																	{/each}
																</tr>
															{/each}
														</tbody>
													</table>
												</div>

												<div class="md:hidden overflow-x-auto p-2">
													<table class="table table-xs w-full text-center border-collapse">
														<thead>
															<tr>
																<th class="border border-base-300 sticky left-0 bg-base-200 w-8 z-10"></th>
																{#each Array(8) as _, d}
																	<th class="font-mono px-1 py-1 border border-base-300 text-[10px] whitespace-nowrap" title={getFullDate(schedule.start, d)}>{formatDateOffset(schedule.start, d)}</th>
																{/each}
															</tr>
														</thead>
														<tbody>
															{#each Array(24) as _, t}
																<tr>
																	<th class="font-mono px-0 py-1 border border-base-300 sticky left-0 bg-base-200 text-[10px] z-10">{t}</th>
																	{#each Array(8) as _, d}
																		<td 
                                                                            class="border border-base-300 transition-colors p-0 cursor-pointer {getHeatmapClass(heatmapData[d][t])}" 
                                                                            title="{formatDateOffset(schedule.start, d)} {t}:00 - Available: {heatmapData[d][t]}"
                                                                            onclick={() => handleCellClick(d, t, schedule.start)}
                                                                        >
																			<div class="w-full h-4 min-w-[2rem] flex items-center justify-center text-[8px] font-bold">
                                                                                {heatmapData[d][t] > 0 ? heatmapData[d][t] : ''}
                                                                            </div>
																		</td>
																	{/each}
																</tr>
															{/each}
														</tbody>
													</table>
												</div>

                                                <!-- Member Drawer Overlay (Right Side) -->
                                                {#if selectedCell}
                                                    <div class="absolute inset-y-0 right-0 w-48 bg-base-100 shadow-2xl border-l border-base-300 z-20 flex flex-col animate-in slide-in-from-right duration-200">
                                                        <div class="p-3 border-b border-base-300 flex justify-between items-center bg-base-200">
                                                            <div>
                                                                <p class="font-bold text-[10px] text-ubuntu">{formatDateOffset(schedule.start, selectedCell.d)}</p>
                                                                <p class="text-[9px] opacity-60 font-mono">{selectedCell.t}:00 ({selectedCell.members.length})</p>
                                                            </div>
                                                            <button class="btn btn-ghost btn-xs btn-circle" onclick={() => selectedCell = null}>✕</button>
                                                        </div>
                                                        <div class="flex-1 overflow-y-auto p-3">
                                                            {#if selectedCell.members.length === 0}
                                                                <p class="text-[10px] opacity-50 italic text-center py-4">No one free.</p>
                                                            {:else}
                                                                <ul class="space-y-1">
                                                                    {#each selectedCell.members as name}
                                                                        <li class="flex items-center gap-2 text-[11px]">
                                                                            <div class="w-1.5 h-1.5 rounded-full bg-success"></div>
                                                                            <span class="font-neo truncate">{name}</span>
                                                                        </li>
                                                                    {/each}
                                                                </ul>
                                                            {/if}
                                                        </div>
                                                    </div>
                                                {/if}
											</div>
										{/if}

										<div class="mt-6 pt-4 border-t border-base-300">
											<div class="flex justify-between items-center mb-3">
												<h5 class="text-md font-bold text-ubuntu">Parties</h5>
												{#if isManager}
													<a href="{base}/schedules/{schedule.id}/parties/create" class="btn btn-xs btn-outline btn-secondary">Add Party</a>
												{/if}
											</div>
											<div class="space-y-2">
												{#each parties.filter(p => p.scheduleId === schedule.id) as party}
													<!-- svelte-ignore a11y_click_events_have_key_events -->
													<!-- svelte-ignore a11y_no_static_element_interactions -->
													<div class="bg-base-200 rounded-md transition-all duration-200 border {expandedPartyId === party.id ? 'border-primary shadow-sm' : 'border-transparent'}">
														<div 
															class="flex justify-between items-center p-3 hover:bg-base-300 cursor-pointer rounded-md"
															onclick={() => toggleParty(party.id, schedule.id)}
														>
															<div class="flex items-center gap-3 overflow-hidden mr-2">
																<span class="font-bold text-sm text-neo truncate">{party.title}</span>
																<span class="text-xs opacity-60 font-mono shrink-0">({party.members}/{party.max})</span>
															</div>
															<div class="flex items-center gap-2 shrink-0">
                                                                {#if isManager}
                                                                    <div class="dropdown dropdown-end dropdown-left" onclick={(e) => e.stopPropagation()}>
                                                                        <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                                        <!-- svelte-ignore a11y_label_has_associated_control -->
                                                                        <label tabindex="0" class="btn btn-ghost btn-xs btn-circle opacity-50 hover:opacity-100">
                                                                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 12.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 18.75a.75.75 0 110-1.5.75.75 0 010 1.5z" /></svg>
                                                                        </label>
                                                                        <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                                        <ul tabindex="0" class="dropdown-content z-[30] menu p-2 shadow bg-base-100 rounded-box w-32 border border-base-200">
                                                                            <li><button class="text-xs" onclick={() => togglePartyStatus(party)}>{party.status === 'Done' ? 'Mark Planned' : 'Mark Done'}</button></li>
                                                                            <li><button class="text-xs text-error" onclick={() => requestDeleteParty(party)}>Delete</button></li>
                                                                        </ul>
                                                                    </div>
                                                                {/if}
																<span class="badge badge-sm badge-outline hidden sm:flex {party.status === 'Done' ? 'badge-success text-white' : ''}">{party.status}</span>
                                                                {#if loadingPartyId === party.id}
                                                                    <span class="loading loading-spinner loading-xs text-primary w-4 h-4"></span>
                                                                {:else}
																    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 transition-transform duration-200 {expandedPartyId === party.id ? 'rotate-180 text-primary' : 'opacity-50'}"><path stroke-linecap="round" stroke-linejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" /></svg>
                                                                {/if}
															</div>
														</div>
														
														{#if expandedPartyId === party.id}
															<div class="p-4 bg-base-100 border-t border-base-300 text-sm rounded-b-md">
																<div class="mb-4">
																	<p class="font-bold text-ubuntu text-xs opacity-60 uppercase mb-1">Start Time</p>
																	<p class="font-mono text-base">{new Date(party.start).toLocaleString()}</p>
																</div>
																
																<div class="mb-4">
																	<p class="font-bold text-ubuntu text-xs opacity-60 uppercase mb-2">Members</p>
																	<div class="flex flex-wrap gap-2">
																		{#each party.joinedMembers as member}
																			<span class="badge badge-neutral">{member}</span>
																		{/each}
																		{#each Array(party.max - party.members) as _}
																			<span class="badge badge-outline border-dashed opacity-40">Empty Slot</span>
																		{/each}
																	</div>
																</div>

                                                                {#if party.status !== 'Done'}
                                                                    <div class="flex justify-end pt-2 border-t border-base-200 mt-4">
                                                                        <div class="w-full sm:w-auto">
                                                                            {#if party.joinedMembers.includes(currentUser?.nickname || currentUser?.username)}
                                                                                <button class="btn btn-sm btn-error btn-outline w-full sm:w-auto px-10" onclick={() => leaveParty(party.id, schedule.id)}>Leave Party</button>
                                                                            {:else}
                                                                                <button class="btn btn-sm btn-primary w-full sm:w-auto px-10" disabled={party.members >= party.max} onclick={() => joinParty(party.id, schedule.id)}>Join Party</button>
                                                                            {/if}
                                                                        </div>
                                                                    </div>
                                                                {/if}
															</div>
														{/if}
													</div>
												{/each}
												{#if parties.filter(p => p.scheduleId === schedule.id).length === 0}
													<p class="text-xs opacity-50 italic text-center py-2">No parties organized yet.</p>
												{/if}
											</div>
										</div>
									</div>
								</div>
							{/each}
						{/if}
					</div>

					<!-- Sidebar Section: Roster -->
					<div class="space-y-6">
						<div class="card bg-base-100 shadow-xl">
							<div class="card-body p-6">
								<h3 class="text-xl font-bold text-ubuntu mb-4">Group Roster</h3>
								<div class="space-y-4">
									{#each members as member}
										<div class="flex items-center justify-between">
											<div class="flex items-center gap-3">
												<div class="avatar placeholder">
													<div class="bg-neutral text-neutral-content rounded-full w-8">
														{#if member.avatar}
															<img src="https://cdn.discordapp.com/avatars/{member.discordId}/{member.avatar}.png" alt={member.username} />
														{:else}
															<span>{member.username.substring(0, 2).toUpperCase()}</span>
														{/if}
													</div>
												</div>
												<div>
													<p class="font-bold text-sm">{member.username}</p>
													<p class="text-[10px] opacity-50 uppercase tracking-wider">{member.role}</p>
												</div>
											</div>

                                            {#if isManager && member.id !== currentUser?.id}
                                                <div class="dropdown dropdown-left {members.indexOf(member) > members.length - 3 ? 'dropdown-top' : 'dropdown-bottom'}">
                                                    <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                    <!-- svelte-ignore a11y_label_has_associated_control -->
                                                    <label tabindex="0" class="btn btn-ghost btn-xs btn-circle opacity-50 hover:opacity-100">
                                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 12.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 18.75a.75.75 0 110-1.5.75.75 0 010 1.5z" /></svg>
                                                    </label>
                                                    <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                    <ul tabindex="0" class="dropdown-content z-[40] menu p-2 shadow bg-base-100 rounded-box w-32 border border-base-200">
                                                        <li><button class="text-xs" onclick={() => notifyMember(member)}>Notify</button></li>
                                                        <li><button class="text-xs" onclick={() => toggleAuditorRole(member)}>{member.role === 'AUDITOR' ? 'Revoke Auditor' : 'Grant Auditor'}</button></li>
                                                        <li><button class="text-xs text-error" onclick={() => requestKickMember(member)}>Kick</button></li>
                                                    </ul>
                                                </div>
                                            {/if}
										</div>
									{/each}
								</div>
							</div>
						</div>
					</div>
				</div>
			{/if}
		</div>
	</main>
</div>

<ConfirmationModal
    bind:this={deleteScheduleModal}
    id="delete-schedule-modal"
    title="Delete Schedule"
    message="Are you sure you want to delete this schedule? All member availability data and organized parties for this schedule will be permanently removed."
    confirmText="Delete Schedule"
    type="error"
    onConfirm={confirmDeleteSchedule}
/>

<ConfirmationModal
    bind:this={deletePartyModal}
    id="delete-party-modal"
    title="Delete Party"
    message="Are you sure you want to delete this party? All joined members will be removed and the party will be permanently deleted."
    confirmText="Delete Party"
    type="error"
    onConfirm={confirmDeleteParty}
/>

<ConfirmationModal
    bind:this={kickMemberModal}
    id="kick-member-modal"
    title="Kick Member"
    message="Are you sure you want to remove {memberToKick?.username} from the group? They will be removed from all future parties and their availability data will be deleted. They can rejoin if they have a new invite code."
    confirmText="Kick Member"
    type="error"
    onConfirm={confirmKickMember}
/>

