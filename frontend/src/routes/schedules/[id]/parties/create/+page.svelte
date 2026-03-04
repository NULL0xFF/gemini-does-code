<script lang="ts">
    import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import { page } from '$app/stores';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { fetchApi, fetchJson, ApiError } from '$lib/api';

	let scheduleId = $derived($page.params.id);
    let schedule = $state<any>(null);
	let title = $state('');
	let raidType = $state('');
	let maxMembers = $state(8);
	
	// Initialize with current date and time, formatted for datetime-local (YYYY-MM-DDThh:mm)
	const getInitialTime = () => {
		const now = new Date();
		now.setMinutes(0, 0, 0);
		return new Date(now.getTime() - (now.getTimezoneOffset() * 60000)).toISOString().slice(0, 16);
	};
	let startTime = $state(getInitialTime());
	
	let isSubmitting = $state(false);

	function logout() {
		localStorage.removeItem('ark_token');
		window.location.href = `${base}/`;
	}

	async function handleSubmit() {
		if (!title.trim() || !startTime) return;

		isSubmitting = true;
		try {
			await fetchApi(`/api/schedules/${scheduleId}/parties`, {
				method: 'POST',
				body: JSON.stringify({
					title,
					raidType: raidType || 'General',
					maxMembers,
					startTime: new Date(startTime).toISOString()
				})
			});

			alert('Party created successfully!');
			window.history.back();
		} catch (err) {
			console.error(err);
			if (!(err instanceof ApiError && err.status === 401)) {
				alert('Failed to create party. Please check your inputs.');
			}
		} finally {
			isSubmitting = false;
		}
	}

    onMount(async () => {
        try {
            // Context might be available in parent, but for standalone deep link, fetch list
            // Note: In a real app, you might have a dedicated GET /api/schedules/{id}
            const schedulesList = await fetchJson<any[]>(`/api/groups/${$page.params.groupId || 'unknown'}/schedules`);
            schedule = schedulesList.find((s: any) => s.id === scheduleId);
        } catch (err) {
            console.error('Failed to load schedule context:', err);
        }
    });
</script>

<svelte:head>
	<title>Create Party - Ark Resolver</title>
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
		<div class="container mx-auto max-w-4xl">
			<div class="flex items-center gap-4 mb-8">
				<button onclick={() => window.history.back()} class="btn btn-ghost btn-circle" aria-label="Go Back">
					<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" /></svg>
				</button>
				<div>
                    <h1 class="text-3xl font-bold text-ubuntu">Create Party</h1>
                    {#if schedule}
                        <p class="opacity-70 font-neo text-sm italic">For: <span class="text-primary font-bold">{schedule.title}</span> ({new Date(schedule.start).toLocaleDateString()} ~ {new Date(schedule.end).toLocaleDateString()})</p>
                    {:else}
                        <p class="opacity-70 font-neo text-sm">Organize a specific raid group.</p>
                    {/if}
                </div>
			</div>

            <div class="space-y-8">
                <!-- Heatmap Reference (Full Width) -->
                <div class="card bg-base-100 shadow-xl border-t-4 border-info">
                    <div class="card-body">
                        <h3 class="card-title text-ubuntu">Heatmap Reference</h3>
                        <p class="text-sm opacity-70 mb-4">Use the aggregated availability to pick the best start time for this party.</p>
                        
                        <div class="w-full h-48 bg-base-200 rounded-lg flex items-center justify-center border border-dashed border-base-300">
                            <span class="opacity-50 text-sm italic">(Heatmap visual placeholder)</span>
                        </div>
                    </div>
                </div>

                <!-- Creation Form -->
                <div class="card bg-base-100 shadow-xl">
                    <div class="card-body">
                        <div class="space-y-6">
                            <div>
                                <label for="title" class="block text-sm font-bold text-ubuntu mb-2">Party Title</label>
                                <input 
                                    id="title"
                                    type="text" 
                                    placeholder="e.g., Valtan Hard Fast Clear" 
                                    class="input input-bordered w-full font-neo focus:ring-primary" 
                                    bind:value={title}
                                />
                            </div>

                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div>
                                    <label for="raid-type" class="block text-sm font-bold text-ubuntu mb-2">Content / Raid Type (Optional)</label>
                                    <input 
                                        id="raid-type"
                                        type="text" 
                                        placeholder="e.g., Valtan, Vykas, Argos" 
                                        class="input input-bordered w-full font-neo focus:ring-primary" 
                                        bind:value={raidType}
                                    />
                                </div>

                                <div>
                                    <label for="max-members" class="block text-sm font-bold text-ubuntu mb-2">Max Members</label>
                                    <div class="join w-full">
                                        <button class="btn join-item" onclick={() => maxMembers = Math.max(1, maxMembers - 1)}>-</button>
                                        <input 
                                            id="max-members"
                                            type="number" 
                                            class="input input-bordered join-item w-full text-center font-mono" 
                                            bind:value={maxMembers}
                                            min="1"
                                            max="40"
                                        />
                                        <button class="btn join-item" onclick={() => maxMembers = Math.min(40, maxMembers + 1)}>+</button>
                                    </div>
                                </div>
                            </div>

                            <div>
                                <label for="start-time" class="block text-sm font-bold text-ubuntu mb-2">Exact Start Time (Local Time)</label>
                                <input 
                                    id="start-time"
                                    type="datetime-local" 
                                    class="input input-bordered w-full font-mono focus:ring-primary" 
                                    bind:value={startTime}
                                />
                                <span class="text-xs opacity-60 mt-1 block">Pick a time when most people are available based on the heatmap.</span>
                            </div>
                        </div>

                        <div class="card-actions justify-end mt-8">
                            <button class="btn btn-ghost" onclick={() => window.history.back()}>Cancel</button>
                            <button 
                                class="btn btn-secondary px-8" 
                                disabled={isSubmitting || !title.trim() || !startTime}
                                onclick={handleSubmit}
                            >
                                {#if isSubmitting}
                                    <span class="loading loading-spinner"></span>
                                {/if}
                                Create Party
                            </button>
                        </div>
                    </div>
                </div>
            </div>
		</div>
	</main>
</div>
