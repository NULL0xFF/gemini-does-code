<script lang="ts">
	import { base } from '$app/paths';
	import { page } from '$app/stores';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { fetchApi, ApiError } from '$lib/api';

	let scheduleId = $derived($page.params.id);
	let isSubmitting = $state(false);

    // Mock schedule data for the UI
    let schedule = $state({
        id: scheduleId,
        title: 'March Week 1 Reset',
        start: '2026-03-04', // YYYY-MM-DD
        days: 8
    });

    // 2D Array to track selected cells [dayIndex][hourIndex]
    let selectedCells = $state<boolean[][]>(Array(8).fill(null).map(() => Array(24).fill(false)));
    let isDragging = $state(false);
    let dragState = $state(false); // true = selecting, false = deselecting

	function logout() {
		localStorage.removeItem('ark_token');
		window.location.href = `${base}/`;
	}

    function formatDateOffset(dateString: string, offsetDays: number) {
		const d = new Date(dateString);
		d.setDate(d.getDate() + offsetDays);
		const month = String(d.getMonth() + 1).padStart(2, '0');
		const day = String(d.getDate()).padStart(2, '0');
		return `${month}-${day}`;
	}

    // Drag-to-select logic
    function handleMouseDown(d: number, t: number) {
        isDragging = true;
        dragState = !selectedCells[d][t];
        selectedCells[d][t] = dragState;
        selectedCells = [...selectedCells]; // trigger reactivity
    }

    function handleMouseEnter(d: number, t: number) {
        if (isDragging) {
            selectedCells[d][t] = dragState;
            selectedCells = [...selectedCells];
        }
    }

    function handleMouseUp() {
        isDragging = false;
    }

	async function handleSubmit() {
		isSubmitting = true;
		try {
            // Convert boolean grid to list of time blocks for backend
            const timeBlocks = [];
            // implementation pending...

			await fetchApi(`/api/schedules/${scheduleId}/availability/me`, {
				method: 'PUT',
				body: JSON.stringify({ blocks: [] })
			});

			alert('Availability submitted successfully! (Mock)');
			window.history.back();
		} catch (err) {
			console.error(err);
			if (err instanceof ApiError && err.status === 404) {
				alert('Availability endpoint not yet implemented.');
			} else if (!(err instanceof ApiError && err.status === 401)) {
				alert('An error occurred while submitting availability.');
			}
		} finally {
			isSubmitting = false;
		}
	}
</script>

<svelte:head>
	<title>Submit Availability - Ark Resolver</title>
</svelte:head>

<!-- Global mouse up handler for drag-to-select -->
<svelte:window onmouseup={handleMouseUp} />

<div class="min-h-screen bg-base-200 flex flex-col font-sans select-none">
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
                    <h1 class="text-3xl font-bold text-ubuntu">Submit Availability</h1>
                    <p class="opacity-70 font-neo text-sm">{schedule.title}</p>
                </div>
			</div>

			<div class="card bg-base-100 shadow-xl">
				<div class="card-body">
                    <p class="text-sm opacity-80 mb-4 font-neo text-center">Click and drag to select the times you are available to raid.</p>
					
                    <!-- Desktop Heatmap (Dates = Rows) -->
                    <div class="hidden md:block overflow-x-auto bg-base-200 p-4 rounded-lg">
                        <table class="table table-xs w-full text-center border-separate border-spacing-1">
                            <thead>
                                <tr>
                                    <th class="border-b border-base-300 w-16"></th>
                                    {#each Array(24) as _, i}
                                        <th class="font-mono px-0 text-xs w-8 pb-2 opacity-60">{i}</th>
                                    {/each}
                                </tr>
                            </thead>
                            <tbody>
                                {#each Array(schedule.days) as _, d}
                                    <tr>
                                        <th class="font-mono whitespace-nowrap px-2 py-2 text-xs opacity-60 text-right pr-4">{formatDateOffset(schedule.start, d)}</th>
                                        {#each Array(24) as _, t}
                                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                            <td 
                                                class="border border-base-300 transition-colors cursor-crosshair p-0 rounded-sm
                                                {selectedCells[d][t] ? 'bg-primary border-primary' : 'bg-base-100 hover:bg-base-300'}"
                                                onmousedown={() => handleMouseDown(d, t)}
                                                onmouseenter={() => handleMouseEnter(d, t)}
                                                title="{formatDateOffset(schedule.start, d)} {t}:00"
                                            >
                                                <div class="w-full h-8"></div>
                                            </td>
                                        {/each}
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    </div>

                    <!-- Mobile Heatmap (Dates = Columns) -->
                    <div class="md:hidden overflow-x-auto bg-base-200 p-2 rounded-lg">
                        <table class="table table-xs w-full text-center border-separate border-spacing-1">
                            <thead>
                                <tr>
                                    <th class="border-b border-base-300 w-8"></th>
                                    {#each Array(schedule.days) as _, d}
                                        <th class="font-mono px-1 pb-2 text-xs opacity-60 whitespace-nowrap">{formatDateOffset(schedule.start, d)}</th>
                                    {/each}
                                </tr>
                            </thead>
                            <tbody>
                                {#each Array(24) as _, t}
                                    <tr>
                                        <th class="font-mono px-2 py-1 text-xs opacity-60 text-right pr-2">{t}</th>
                                        {#each Array(schedule.days) as _, d}
                                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                            <td 
                                                class="border border-base-300 transition-colors cursor-crosshair p-0 rounded-sm
                                                {selectedCells[d][t] ? 'bg-primary border-primary' : 'bg-base-100 hover:bg-base-300'}"
                                                onmousedown={() => handleMouseDown(d, t)}
                                                onmouseenter={() => handleMouseEnter(d, t)}
                                                title="{formatDateOffset(schedule.start, d)} {t}:00"
                                            >
                                                <div class="w-full h-8 min-w-[3rem]"></div>
                                            </td>
                                        {/each}
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    </div>

					<div class="card-actions justify-end mt-8">
						<button class="btn btn-ghost" onclick={() => window.history.back()}>Cancel</button>
						<button 
							class="btn btn-primary px-8" 
							disabled={isSubmitting}
							onclick={handleSubmit}
						>
							{#if isSubmitting}
								<span class="loading loading-spinner"></span>
							{/if}
							Save Availability
						</button>
					</div>
				</div>
			</div>
		</div>
	</main>
</div>
