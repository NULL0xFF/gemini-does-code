<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import { page } from '$app/stores';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { fetchJson, ApiError } from '$lib/api';

	let groupId = $derived($page.params.id);
	let group = $state<any>(null);
	let isLoading = $state(true);
	let error = $state('');

	// Mock data for the UI before backend implementation
	let isManager = $state(true); 
	let members = $state([
		{ id: '1', username: 'NULL0xFF', role: 'MANAGER' },
		{ id: '2', username: 'MokoKoko', role: 'MEMBER' },
		{ id: '3', username: 'BardLife', role: 'MEMBER' }
	]);
	let schedules = $state([
		{ id: '1', title: 'March Week 1 Reset', start: '2026-03-04 10:00', end: '2026-03-11 05:00', status: 'ACTIVE' },
		{ id: '2', title: 'March Week 2 Reset', start: '2026-03-11 10:00', end: '2026-03-18 05:00', status: 'PLANNED' }
	]);
	
	let openHeatmapId = $state<string | null>(null);
	let expandedPartyId = $state<string | null>(null);

	let parties = $state([
		{ 
			id: 'p1', scheduleId: '1', title: 'Valtan HM Fast', members: 4, max: 8, status: 'On-going', start: '2026-03-05 20:00',
			joinedMembers: ['NULL0xFF', 'MokoKoko', 'BardLife', 'GunlancerPro'] 
		},
		{ 
			id: 'p2', scheduleId: '1', title: 'Biackiss NM Learning', members: 6, max: 8, status: 'Planned', start: '2026-03-06 21:00',
			joinedMembers: ['NULL0xFF', 'Newbie1', 'Newbie2', 'Newbie3', 'Newbie4', 'MokoKoko'] 
		}
	]);

	function toggleHeatmap(id: string) {
		openHeatmapId = openHeatmapId === id ? null : id;
	}

	function toggleParty(id: string) {
		expandedPartyId = expandedPartyId === id ? null : id;
	}

	function formatDateOffset(dateString: string, offsetDays: number) {
		const d = new Date(dateString);
		d.setDate(d.getDate() + offsetDays);
		const month = String(d.getMonth() + 1).padStart(2, '0');
		const day = String(d.getDate()).padStart(2, '0');
		return `${month}-${day}`;
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

		try {
			// group = await fetchJson<any>(`/api/groups/${groupId}`);
			// Mocking group fetch
			setTimeout(() => {
				group = {
					id: groupId,
					name: 'Friday Night Static',
					description: 'Weekly hard mode clears. Be geared and ready by 8 PM KST.',
					createdAt: '2026-03-01T12:00:00Z'
				};
				isLoading = false;
			}, 500);
		} catch (err) {
			console.error(err);
			if (!(err instanceof ApiError && err.status === 401)) {
				error = 'Failed to load group details.';
				isLoading = false;
			}
		}
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
						{#if isManager}
							<div class="flex gap-2">
								<a href="{base}/groups/{groupId}/invites" class="btn btn-primary">Invite Members</a>
								<a href="{base}/groups/{groupId}/invites" class="btn btn-outline">Settings</a>
							</div>
						{/if}
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
									<div class="card-body p-5">
										<div class="flex justify-between items-center">
											<div>
												<div class="flex items-center gap-2">
													<h4 class="text-lg font-bold text-ubuntu">{schedule.title}</h4>
													<div class="badge {schedule.status === 'ACTIVE' ? 'badge-success text-white' : 'badge-neutral'}">
														{schedule.status}
													</div>
												</div>
												<p class="text-sm opacity-70 mt-1 font-mono">{schedule.start} ~ {schedule.end}</p>
											</div>
											<div class="flex gap-2">
												<a href="{base}/schedules/{schedule.id}/availability" class="btn btn-sm btn-outline hidden sm:flex">Submit Availability</a>
												<button class="btn btn-sm btn-primary" onclick={() => toggleHeatmap(schedule.id)}>
													{openHeatmapId === schedule.id ? 'Hide Heatmap' : 'View Heatmap'}
												</button>
											</div>
										</div>
										<a href="{base}/schedules/{schedule.id}/availability" class="btn btn-sm btn-outline w-full mt-2 sm:hidden">Submit Availability</a>

										{#if openHeatmapId === schedule.id}
											<div class="mt-4 bg-base-200 p-2 rounded-lg">
												<div class="hidden md:block overflow-x-auto">
													<table class="table table-xs w-full text-center border-separate border-spacing-0">
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
																	<th class="font-mono whitespace-nowrap px-1 py-1 border border-base-300 sticky left-0 bg-base-200 text-[10px] z-10">{formatDateOffset(schedule.start, d)}</th>
																	{#each Array(24) as _, t}
																		<td class="border border-base-300 bg-base-100 hover:bg-primary/50 transition-colors cursor-pointer p-0" title="{formatDateOffset(schedule.start, d)} {t}:00">
																			<div class="w-full h-4"></div>
																		</td>
																	{/each}
																</tr>
															{/each}
														</tbody>
													</table>
												</div>

												<div class="md:hidden overflow-x-auto">
													<table class="table table-xs w-full text-center border-separate border-spacing-0">
														<thead>
															<tr>
																<th class="border border-base-300 sticky left-0 bg-base-200 w-8 z-10"></th>
																{#each Array(8) as _, d}
																	<th class="font-mono px-1 py-1 border border-base-300 text-[10px] whitespace-nowrap">{formatDateOffset(schedule.start, d)}</th>
																{/each}
															</tr>
														</thead>
														<tbody>
															{#each Array(24) as _, t}
																<tr>
																	<th class="font-mono px-0 py-1 border border-base-300 sticky left-0 bg-base-200 text-[10px] z-10">{t}</th>
																	{#each Array(8) as _, d}
																		<td class="border border-base-300 bg-base-100 hover:bg-primary/50 transition-colors cursor-pointer p-0" title="{formatDateOffset(schedule.start, d)} {t}:00">
																			<div class="w-full h-4 min-w-[2rem]"></div>
																		</td>
																	{/each}
																</tr>
															{/each}
														</tbody>
													</table>
												</div>
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
													<div class="bg-base-200 rounded-md overflow-hidden transition-all duration-200 border {expandedPartyId === party.id ? 'border-primary shadow-sm' : 'border-transparent'}">
														<div 
															class="flex justify-between items-center p-3 hover:bg-base-300 cursor-pointer"
															onclick={() => toggleParty(party.id)}
														>
															<div class="flex items-center gap-3">
																<span class="font-bold text-sm text-neo">{party.title}</span>
																<span class="text-xs opacity-60 font-mono">({party.members}/{party.max})</span>
															</div>
															<div class="flex items-center gap-2">
																<span class="badge badge-sm badge-outline">{party.status}</span>
																<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 transition-transform duration-200 {expandedPartyId === party.id ? 'rotate-180 text-primary' : 'opacity-50'}"><path stroke-linecap="round" stroke-linejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" /></svg>
															</div>
														</div>
														
														{#if expandedPartyId === party.id}
															<div class="p-4 bg-base-100 border-t border-base-300 text-sm">
																<div class="flex justify-between items-start mb-4">
																	<div>
																		<p class="font-bold text-ubuntu text-xs opacity-60 uppercase mb-1">Start Time</p>
																		<p class="font-mono text-base">{party.start}</p>
																	</div>
																	<button class="btn btn-sm btn-primary px-6">Join</button>
																</div>
																
																<div>
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
															</div>
														{/if}
													</div>
												{/each}
												{#if parties.filter(p => p.scheduleId === schedule.id).length === 0}
													<p class="text-sm opacity-50 italic">No parties created yet for this schedule.</p>
												{/if}
											</div>
										</div>
									</div>
								</div>
							{/each}
						{/if}
					</div>

					<!-- Sidebar: Members -->
					<div class="space-y-6">
						<h3 class="text-2xl font-bold text-ubuntu">Members ({members.length})</h3>
						<div class="card bg-base-100 shadow-md">
							<div class="card-body p-0">
								<ul class="menu bg-base-100 w-full rounded-box">
									{#each members as member}
										<li>
											<div class="flex justify-between hover:bg-base-200">
												<span class="font-neo font-bold">{member.username}</span>
												{#if member.role === 'MANAGER'}
													<span class="badge badge-primary badge-sm text-xs">Manager</span>
												{/if}
											</div>
										</li>
									{/each}
								</ul>
							</div>
						</div>
					</div>
				</div>
			{/if}
		</div>
	</main>
</div>
