<script lang="ts">
    import { onMount } from 'svelte';
    import { base } from '$app/paths';
    import { page } from '$app/stores';
    import ConfirmationModal from '$lib/components/ConfirmationModal.svelte';
    import { fetchApi, fetchJson, ApiError } from '$lib/api';
    import { auth, getAvatarUrl } from '$lib/stores/auth.svelte';
    import { toast } from '$lib/stores/toast.svelte';
    import type { GroupResponse, GroupMemberResponse, ScheduleResponse, PartyResponse, AvailabilityResponse } from '$lib/types/api';

    let groupId = $derived($page.params.id);
    let group = $state<GroupResponse | null>(null);
    let members = $state<GroupMemberResponse[]>([]);
    let schedules = $state<ScheduleResponse[]>([]);
    let parties = $state<PartyResponse[]>([]);
    let rawAvailability = $state<AvailabilityResponse[]>([]);
    let heatmapData = $state<number[][] | null>(null);

    let isLoading = $state(true);
    let error = $state('');

    let openHeatmapId = $state<string | null>(null);
    let expandedPartyId = $state<string | null>(null);
    let loadingPartyId = $state<string | null>(null);
    let selectedCell = $state<{ d: number; t: number; members: string[] } | null>(null);

    let isManager = $derived(members.find(m => m.id === auth.user?.id)?.role === 'MANAGER');
    let isAuditor = $derived(members.find(m => m.id === auth.user?.id)?.role === 'AUDITOR');
    let isAdmin = $derived(isManager || isAuditor);

    let deleteScheduleModal: ReturnType<typeof ConfirmationModal>;
    let scheduleToDelete = $state<string | null>(null);

    let deletePartyModal: ReturnType<typeof ConfirmationModal>;
    let partyToDelete = $state<PartyResponse | null>(null);

    let kickMemberModal: ReturnType<typeof ConfirmationModal>;
    let memberToKick = $state<GroupMemberResponse | null>(null);

    async function fetchData() {
        try {
            const [groupData, memberData, scheduleData] = await Promise.all([
                fetchJson<GroupResponse>(`/api/groups/${groupId}`),
                fetchJson<GroupMemberResponse[]>(`/api/groups/${groupId}/members`),
                fetchJson<ScheduleResponse[]>(`/api/groups/${groupId}/schedules`)
            ]);
            group = groupData;
            members = memberData;
            schedules = scheduleData;
            if (schedules.length > 0) {
                await Promise.all(schedules.map(s => fetchParties(s.id)));
            }
        } catch (err) {
            if (!(err instanceof ApiError && err.status === 401)) {
                error = 'Failed to load group details.';
            }
        } finally {
            isLoading = false;
        }
    }

    async function fetchParties(scheduleId: string) {
        try {
            const data = await fetchJson<PartyResponse[]>(`/api/schedules/${scheduleId}/parties`);
            parties = [...parties.filter(p => p.scheduleId !== scheduleId), ...data.map(p => ({ ...p, scheduleId }))];
        } catch {
            // non-fatal; parties section will show empty
        }
    }

    async function fetchHeatmap(scheduleId: string, startDate: string) {
        try {
            const data = await fetchJson<AvailabilityResponse[]>(`/api/schedules/${scheduleId}/availability`);
            rawAvailability = data;

            const grid = Array.from({ length: 8 }, () => Array(24).fill(0));
            const baseDate = new Date(startDate);
            baseDate.setHours(0, 0, 0, 0);

            for (const userAvail of data) {
                for (const block of userAvail.blocks) {
                    const diffHours = Math.floor((new Date(block.start).getTime() - baseDate.getTime()) / 3_600_000);
                    const d = Math.floor(diffHours / 24);
                    const t = diffHours % 24;
                    if (d >= 0 && d < 8 && t >= 0 && t < 24) grid[d][t]++;
                }
            }
            heatmapData = grid;
        } catch {
            // non-fatal
        }
    }

    async function toggleHeatmap(id: string, startDate: string) {
        if (openHeatmapId !== id) await fetchHeatmap(id, startDate);
        openHeatmapId = openHeatmapId === id ? null : id;
        selectedCell = null;
    }

    async function renameSchedule(scheduleId: string, oldTitle: string) {
        const schedule = schedules.find(s => s.id === scheduleId);
        const newTitle = prompt(`Enter new title for "${oldTitle}":`, oldTitle);
        if (!newTitle || newTitle === oldTitle || !schedule) return;
        try {
            await fetchApi(`/api/schedules/${scheduleId}`, {
                method: 'PUT',
                body: JSON.stringify({ title: newTitle, startTime: schedule.start, endTime: schedule.end })
            });
            await fetchData();
            toast.success('Schedule renamed.');
        } catch {
            toast.error('Failed to rename schedule.');
        }
    }

    function requestDeleteSchedule(scheduleId: string) {
        scheduleToDelete = scheduleId;
        deleteScheduleModal.show();
    }

    async function confirmDeleteSchedule() {
        if (!scheduleToDelete) return;
        try {
            await fetchApi(`/api/schedules/${scheduleToDelete}`, { method: 'DELETE' });
            await fetchData();
            toast.success('Schedule deleted.');
        } catch {
            toast.error('Failed to delete schedule.');
        } finally {
            scheduleToDelete = null;
        }
    }

    async function toggleParty(id: string, scheduleId: string) {
        if (expandedPartyId !== id) {
            loadingPartyId = id;
            await fetchParties(scheduleId);
            loadingPartyId = null;
        }
        expandedPartyId = expandedPartyId === id ? null : id;
    }

    function requestDeleteParty(party: PartyResponse) {
        partyToDelete = party;
        deletePartyModal.show();
    }

    async function confirmDeleteParty() {
        if (!partyToDelete) return;
        try {
            await fetchApi(`/api/parties/${partyToDelete.id}`, { method: 'DELETE' });
            await fetchParties(partyToDelete.scheduleId);
            toast.success('Party deleted.');
        } catch {
            toast.error('Failed to delete party.');
        } finally {
            partyToDelete = null;
        }
    }

    async function togglePartyStatus(party: PartyResponse) {
        try {
            const completed = party.status !== 'Done';
            await fetchApi(`/api/parties/${party.id}/complete`, {
                method: 'PATCH',
                body: JSON.stringify({ completed })
            });
            await fetchParties(party.scheduleId);
            toast.success(completed ? 'Party marked as Done.' : 'Party marked as Planned.');
        } catch {
            toast.error('Failed to update party status.');
        }
    }

    async function joinParty(partyId: string, scheduleId: string) {
        try {
            await fetchApi(`/api/parties/${partyId}/join`, { method: 'POST' });
            await fetchParties(scheduleId);
            toast.success('Joined the party!');
        } catch {
            await fetchParties(scheduleId);
            toast.error('Failed to join. Syncing latest data...');
        }
    }

    async function leaveParty(partyId: string, scheduleId: string) {
        try {
            await fetchApi(`/api/parties/${partyId}/leave`, { method: 'POST' });
            await fetchParties(scheduleId);
            toast.success('Left the party.');
        } catch {
            await fetchParties(scheduleId);
            toast.error('Failed to leave. Syncing latest data...');
        }
    }

    function requestKickMember(member: GroupMemberResponse) {
        memberToKick = member;
        kickMemberModal.show();
    }

    async function confirmKickMember() {
        if (!memberToKick) return;
        try {
            await fetchApi(`/api/groups/${groupId}/members/${memberToKick.id}`, { method: 'DELETE' });
            await fetchData();
            toast.success(`${memberToKick.username} has been removed from the group.`);
        } catch {
            toast.error('Failed to remove member.');
        } finally {
            memberToKick = null;
        }
    }

    async function toggleAuditorRole(member: GroupMemberResponse) {
        const newRole = member.role === 'AUDITOR' ? 'MEMBER' : 'AUDITOR';
        try {
            await fetchApi(`/api/groups/${groupId}/members/${member.id}/role`, {
                method: 'PATCH',
                body: JSON.stringify({ role: newRole })
            });
            await fetchData();
            toast.success(`${member.username} is now ${newRole}.`);
        } catch {
            toast.error('Failed to update role.');
        }
    }

    function notifyMember(member: GroupMemberResponse) {
        toast.info(`Notification feature for ${member.username} is planned.`);
    }

    function formatDateOffset(dateString: string, offsetDays: number) {
        const d = new Date(dateString);
        d.setDate(d.getDate() + offsetDays);
        const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
        return `${String(d.getDate()).padStart(2, '0')} (${weekdays[d.getDay()]})`;
    }

    function getFullDate(dateString: string, offsetDays: number) {
        const d = new Date(dateString);
        d.setDate(d.getDate() + offsetDays);
        return d.toLocaleDateString();
    }

    function getHeatmapClass(count: number) {
        if (!count) return 'bg-base-100';
        const ratio = count / Math.max(members.length, 1);
        if (ratio >= 0.8) return 'bg-success text-success-content';
        if (ratio >= 0.5) return 'bg-primary text-primary-content';
        if (ratio >= 0.2) return 'bg-info text-info-content';
        return 'bg-warning text-warning-content';
    }

    function handleCellClick(d: number, t: number, scheduleStart: string) {
        const baseDate = new Date(scheduleStart);
        baseDate.setHours(0, 0, 0, 0);
        const targetTime = baseDate.getTime() + (d * 24 + t) * 3_600_000;

        const availMembers = rawAvailability
            .filter(ua => ua.blocks.some(b => new Date(b.start).getTime() === targetTime))
            .map(ua => members.find(m => m.id === ua.userId)?.username ?? 'Unknown');

        selectedCell = { d, t, members: availMembers };
    }

    onMount(fetchData);
</script>

<svelte:head>
    <title>{group ? group.name : 'Loading...'} - Ark Resolver</title>
</svelte:head>

<main class="flex-1 p-4 md:p-8">
    <div class="container mx-auto max-w-6xl">

        <!-- Page header -->
        <div class="flex items-center gap-4 mb-8">
            <a href="{base}/dashboard" class="btn btn-ghost btn-circle" aria-label="Back to Dashboard">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
                </svg>
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
            <!-- Group hero card -->
            <div class="card bg-base-100 shadow-xl mb-8 border-l-4 border-primary">
                <div class="card-body flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                    <div class="min-w-0">
                        <h2 class="text-4xl font-bold text-ubuntu mb-1">{group.name}</h2>
                        {#if group.description}
                            <p class="opacity-70 font-neo">{group.description}</p>
                        {/if}
                    </div>
                    <div class="flex gap-2 shrink-0">
                        {#if isAdmin}
                            <a href="{base}/groups/{groupId}/invites" class="btn btn-primary">Invite Members</a>
                        {/if}
                        <a href="{base}/groups/{groupId}/settings" class="btn btn-outline font-bold">Settings</a>
                    </div>
                </div>
            </div>

            <!-- Main content grid -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

                <!-- ── Schedules column ────────────────────────────────── -->
                <div class="lg:col-span-2 space-y-6">
                    <div class="flex items-center justify-between">
                        <h3 class="text-2xl font-bold text-ubuntu">Schedules</h3>
                        {#if isAdmin}
                            <a href="{base}/groups/{groupId}/schedules/create" class="btn btn-sm btn-secondary">New Schedule</a>
                        {/if}
                    </div>

                    {#if schedules.length === 0}
                        <div class="card bg-base-100 shadow-md">
                            <div class="card-body text-center opacity-60 py-12">No schedules have been created yet.</div>
                        </div>
                    {:else}
                        {#each schedules as schedule}
                            {@const scheduleParties = parties.filter(p => p.scheduleId === schedule.id)}
                            <div class="card bg-base-100 shadow-md hover:shadow-lg transition-shadow border-l-4 {schedule.status === 'ACTIVE' ? 'border-success' : 'border-neutral'}">
                                <div class="card-body p-5 gap-4">

                                    <!-- Schedule header -->
                                    <div class="flex items-start justify-between gap-4">
                                        <div class="min-w-0">
                                            <div class="flex items-center gap-2 flex-wrap">
                                                <h4 class="text-lg font-bold text-ubuntu">{schedule.title}</h4>
                                                <span class="badge badge-sm {schedule.status === 'ACTIVE' ? 'badge-success text-white' : 'badge-neutral'}">{schedule.status}</span>
                                            </div>
                                            <p class="text-xs opacity-60 font-mono mt-1">
                                                {new Date(schedule.start).toLocaleString()} — {new Date(schedule.end).toLocaleString()}
                                            </p>
                                        </div>
                                        <div class="flex items-center gap-1 shrink-0">
                                            <a href="{base}/schedules/{schedule.id}/availability" class="btn btn-sm btn-outline">Submit Availability</a>
                                            <button class="btn btn-sm btn-primary" onclick={() => toggleHeatmap(schedule.id, schedule.start)}>
                                                {openHeatmapId === schedule.id ? 'Hide Heatmap' : 'View Heatmap'}
                                            </button>
                                            {#if isAdmin}
                                                <div class="dropdown dropdown-end">
                                                    <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                    <!-- svelte-ignore a11y_label_has_associated_control -->
                                                    <label tabindex="0" class="btn btn-ghost btn-sm btn-circle">
                                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4">
                                                            <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM12.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0zM18.75 12a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
                                                        </svg>
                                                    </label>
                                                    <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                    <ul tabindex="0" class="dropdown-content z-10 menu p-2 shadow bg-base-100 rounded-box w-32 border border-base-200">
                                                        <li><button onclick={() => renameSchedule(schedule.id, schedule.title)}>Rename</button></li>
                                                        <li><button class="text-error" onclick={() => requestDeleteSchedule(schedule.id)}>Delete</button></li>
                                                    </ul>
                                                </div>
                                            {/if}
                                        </div>
                                    </div>

                                    <!-- Heatmap -->
                                    {#if openHeatmapId === schedule.id && heatmapData}
                                        <div class="bg-base-200 rounded-lg border border-base-300 overflow-hidden">

                                            <!-- Desktop: days as rows, hours as columns -->
                                            <div class="hidden md:block overflow-x-auto p-2">
                                                <table class="table table-xs w-full text-center border-collapse">
                                                    <thead>
                                                        <tr>
                                                            <th class="border border-base-300 sticky left-0 bg-base-200 w-14 z-10"></th>
                                                            {#each Array(24) as _, i}
                                                                <th class="font-mono border border-base-300 text-[10px] w-5 px-0">{i}</th>
                                                            {/each}
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {#each Array(8) as _, d}
                                                            <tr>
                                                                <th class="font-mono whitespace-nowrap px-1 py-1 border border-base-300 sticky left-0 bg-base-200 text-[10px] z-10" title={getFullDate(schedule.start, d)}>
                                                                    {formatDateOffset(schedule.start, d)}
                                                                </th>
                                                                {#each Array(24) as _, t}
                                                                    <!-- svelte-ignore a11y_click_events_have_key_events -->
                                                                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                                                    <td
                                                                        class="border border-base-300 p-0 cursor-pointer transition-colors {getHeatmapClass(heatmapData[d][t])}"
                                                                        title="{getFullDate(schedule.start, d)} {t}:00 — {heatmapData[d][t]} available"
                                                                        onclick={() => handleCellClick(d, t, schedule.start)}
                                                                    >
                                                                        <div class="w-full h-4 flex items-center justify-center text-[8px] font-bold">
                                                                            {heatmapData[d][t] > 0 ? heatmapData[d][t] : ''}
                                                                        </div>
                                                                    </td>
                                                                {/each}
                                                            </tr>
                                                        {/each}
                                                    </tbody>
                                                </table>
                                            </div>

                                            <!-- Mobile: hours as rows, days as columns -->
                                            <div class="md:hidden overflow-x-auto p-2">
                                                <table class="table table-xs w-full text-center border-collapse">
                                                    <thead>
                                                        <tr>
                                                            <th class="border border-base-300 sticky left-0 bg-base-200 w-8 z-10"></th>
                                                            {#each Array(8) as _, d}
                                                                <th class="font-mono px-1 py-1 border border-base-300 text-[10px] whitespace-nowrap" title={getFullDate(schedule.start, d)}>
                                                                    {formatDateOffset(schedule.start, d)}
                                                                </th>
                                                            {/each}
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {#each Array(24) as _, t}
                                                            <tr>
                                                                <th class="font-mono px-0 py-1 border border-base-300 sticky left-0 bg-base-200 text-[10px] z-10">{t}</th>
                                                                {#each Array(8) as _, d}
                                                                    <!-- svelte-ignore a11y_click_events_have_key_events -->
                                                                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                                                    <td
                                                                        class="border border-base-300 p-0 cursor-pointer transition-colors {getHeatmapClass(heatmapData[d][t])}"
                                                                        title="{formatDateOffset(schedule.start, d)} {t}:00 — {heatmapData[d][t]} available"
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

                                            <!-- Selected cell details (below table, not overlapping) -->
                                            {#if selectedCell}
                                                <div class="border-t border-base-300 bg-base-100 px-4 py-3 flex flex-wrap items-center gap-x-6 gap-y-2">
                                                    <div>
                                                        <p class="text-xs font-bold text-ubuntu">{getFullDate(schedule.start, selectedCell.d)}</p>
                                                        <p class="text-xs font-mono opacity-60">{selectedCell.t}:00 — {selectedCell.members.length} available</p>
                                                    </div>
                                                    <div class="flex flex-wrap gap-1 flex-1 min-w-0">
                                                        {#if selectedCell.members.length === 0}
                                                            <span class="text-xs opacity-50 italic">No one available at this time.</span>
                                                        {:else}
                                                            {#each selectedCell.members as name}
                                                                <span class="badge badge-sm badge-success">{name}</span>
                                                            {/each}
                                                        {/if}
                                                    </div>
                                                    <button class="btn btn-ghost btn-xs btn-circle ml-auto" onclick={() => selectedCell = null} aria-label="Close">✕</button>
                                                </div>
                                            {/if}
                                        </div>
                                    {/if}

                                    <!-- Parties section -->
                                    <div class="border-t border-base-300 pt-4">
                                        <div class="flex items-center justify-between mb-3">
                                            <h5 class="font-bold text-ubuntu">Parties</h5>
                                            {#if isAdmin}
                                                <a href="{base}/schedules/{schedule.id}/parties/create" class="btn btn-xs btn-outline btn-secondary">Add Party</a>
                                            {/if}
                                        </div>

                                        {#if scheduleParties.length === 0}
                                            <p class="text-xs opacity-50 italic text-center py-3">No parties organized yet.</p>
                                        {:else}
                                            <div class="space-y-2">
                                                {#each scheduleParties as party}
                                                    <div class="rounded-lg border transition-colors {expandedPartyId === party.id ? 'border-primary bg-base-200' : 'border-base-300 bg-base-200'}">

                                                        <!-- Party row header -->
                                                        <!-- svelte-ignore a11y_click_events_have_key_events -->
                                                        <!-- svelte-ignore a11y_no_static_element_interactions -->
                                                        <div
                                                            class="flex items-center justify-between px-4 py-3 cursor-pointer hover:bg-base-300 rounded-lg"
                                                            onclick={() => toggleParty(party.id, schedule.id)}
                                                        >
                                                            <div class="flex items-center gap-3 min-w-0">
                                                                <span class="font-bold text-sm font-neo truncate">{party.title}</span>
                                                                <span class="text-xs opacity-60 font-mono shrink-0">{party.members}/{party.max}</span>
                                                                <span class="badge badge-sm {party.status === 'Done' ? 'badge-success text-white' : 'badge-outline'} hidden sm:flex">{party.status}</span>
                                                            </div>
                                                            <div class="flex items-center gap-2 shrink-0">
                                                                {#if isAdmin}
                                                                    <div class="dropdown dropdown-end" onclick={(e) => e.stopPropagation()}>
                                                                        <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                                        <!-- svelte-ignore a11y_label_has_associated_control -->
                                                                        <label tabindex="0" class="btn btn-ghost btn-xs btn-circle">
                                                                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4">
                                                                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 12.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 18.75a.75.75 0 110-1.5.75.75 0 010 1.5z" />
                                                                            </svg>
                                                                        </label>
                                                                        <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                                        <ul tabindex="0" class="dropdown-content z-30 menu p-2 shadow bg-base-100 rounded-box w-36 border border-base-200">
                                                                            <li><button onclick={() => togglePartyStatus(party)}>{party.status === 'Done' ? 'Mark Planned' : 'Mark Done'}</button></li>
                                                                            <li><button class="text-error" onclick={() => requestDeleteParty(party)}>Delete</button></li>
                                                                        </ul>
                                                                    </div>
                                                                {/if}
                                                                {#if loadingPartyId === party.id}
                                                                    <span class="loading loading-spinner loading-xs text-primary"></span>
                                                                {:else}
                                                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 transition-transform duration-200 {expandedPartyId === party.id ? 'rotate-180 text-primary' : 'opacity-40'}">
                                                                        <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
                                                                    </svg>
                                                                {/if}
                                                            </div>
                                                        </div>

                                                        <!-- Party expanded details -->
                                                        {#if expandedPartyId === party.id}
                                                            <div class="px-4 pb-4 border-t border-base-300 bg-base-100 rounded-b-lg">
                                                                <div class="pt-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
                                                                    <div>
                                                                        <p class="text-xs font-bold opacity-60 uppercase tracking-wider mb-1">Start Time</p>
                                                                        <p class="font-mono text-sm">{new Date(party.start).toLocaleString()}</p>
                                                                    </div>
                                                                    <div>
                                                                        <p class="text-xs font-bold opacity-60 uppercase tracking-wider mb-2">Members</p>
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

                                                                {#if party.status !== 'Done'}
                                                                    <div class="flex justify-end mt-4 pt-3 border-t border-base-200">
                                                                        {#if party.joinedMembers.includes(auth.user?.nickname || auth.user?.username || '')}
                                                                            <button class="btn btn-sm btn-error btn-outline" onclick={() => leaveParty(party.id, schedule.id)}>Leave Party</button>
                                                                        {:else}
                                                                            <button class="btn btn-sm btn-primary" disabled={party.members >= party.max} onclick={() => joinParty(party.id, schedule.id)}>Join Party</button>
                                                                        {/if}
                                                                    </div>
                                                                {/if}
                                                            </div>
                                                        {/if}
                                                    </div>
                                                {/each}
                                            </div>
                                        {/if}
                                    </div>

                                </div>
                            </div>
                        {/each}
                    {/if}
                </div>

                <!-- ── Roster sidebar ──────────────────────────────────── -->
                <div class="space-y-6">
                    <div class="card bg-base-100 shadow-xl">
                        <div class="card-body p-6">
                            <h3 class="text-xl font-bold text-ubuntu mb-4">Group Roster</h3>
                            <div class="space-y-3">
                                {#each members as member, i}
                                    <div class="flex items-center justify-between gap-2">
                                        <div class="flex items-center gap-3 min-w-0">
                                            <div class="avatar">
                                                <div class="w-9 rounded-full ring ring-base-300 ring-offset-base-100 ring-offset-1">
                                                    <img src={getAvatarUrl(member.discordId, member.avatar)} alt={member.username} />
                                                </div>
                                            </div>
                                            <div class="min-w-0">
                                                <p class="font-bold text-sm truncate">{member.username}</p>
                                                <p class="text-[10px] opacity-50 uppercase tracking-wider">{member.role}</p>
                                            </div>
                                        </div>

                                        {#if isAdmin && member.id !== auth.user?.id}
                                            <div class="dropdown dropdown-left {i >= members.length - 2 ? 'dropdown-top' : 'dropdown-bottom'}">
                                                <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                <!-- svelte-ignore a11y_label_has_associated_control -->
                                                <label tabindex="0" class="btn btn-ghost btn-xs btn-circle opacity-50 hover:opacity-100">
                                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4">
                                                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 12.75a.75.75 0 110-1.5.75.75 0 010 1.5zM12 18.75a.75.75 0 110-1.5.75.75 0 010 1.5z" />
                                                    </svg>
                                                </label>
                                                <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
                                                <ul tabindex="0" class="dropdown-content z-40 menu p-2 shadow bg-base-100 rounded-box w-36 border border-base-200">
                                                    <li><button onclick={() => notifyMember(member)}>Notify</button></li>
                                                    {#if isManager}
                                                        <li><button onclick={() => toggleAuditorRole(member)}>{member.role === 'AUDITOR' ? 'Revoke Auditor' : 'Grant Auditor'}</button></li>
                                                        <li><button class="text-error" onclick={() => requestKickMember(member)}>Kick</button></li>
                                                    {/if}
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

<ConfirmationModal
    bind:this={deleteScheduleModal}
    id="delete-schedule-modal"
    title="Delete Schedule"
    message="Are you sure you want to delete this schedule? All member availability data and organized parties will be permanently removed."
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
    message="Are you sure you want to remove {memberToKick?.username} from the group? They will be removed from all parties and their availability data will be deleted."
    confirmText="Kick Member"
    type="error"
    onConfirm={confirmKickMember}
/>
