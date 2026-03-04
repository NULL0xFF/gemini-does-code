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
	
	let dragStart = $state<{d: number, t: number} | null>(null);
	let dragCurrent = $state<{d: number, t: number} | null>(null);

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

    // Square selection logic
    function handleMouseDown(d: number, t: number) {
        isDragging = true;
        dragState = !selectedCells[d][t];
		dragStart = { d, t };
		dragCurrent = { d, t };
    }

    function handleMouseEnter(d: number, t: number) {
        if (isDragging) {
			dragCurrent = { d, t };
        }
    }

    function handleMouseUp() {
        if (isDragging && dragStart && dragCurrent) {
			const minD = Math.min(dragStart.d, dragCurrent.d);
			const maxD = Math.max(dragStart.d, dragCurrent.d);
			const minT = Math.min(dragStart.t, dragCurrent.t);
			const maxT = Math.max(dragStart.t, dragCurrent.t);

			// Apply the selection to the grid
			for (let d = minD; d <= maxD; d++) {
				for (let t = minT; t <= maxT; t++) {
					selectedCells[d][t] = dragState;
				}
			}
			selectedCells = [...selectedCells];
		}
		
		isDragging = false;
		dragStart = null;
		dragCurrent = null;
    }

	// Helper to determine if a cell is currently inside the visual drag preview box
	function isInDragBox(d: number, t: number) {
		if (!isDragging || !dragStart || !dragCurrent) return false;
		const minD = Math.min(dragStart.d, dragCurrent.d);
		const maxD = Math.max(dragStart.d, dragCurrent.d);
		const minT = Math.min(dragStart.t, dragCurrent.t);
		const maxT = Math.max(dragStart.t, dragCurrent.t);
		return d >= minD && d <= maxD && t >= minT && t <= maxT;
	}

	async function handleSubmit() {
		isSubmitting = true;
		try {
            // Convert boolean grid to list of time blocks for backend
            const timeBlocks = [];
            const startDate = new Date(schedule.start);
            startDate.setHours(0, 0, 0, 0); // Start from beginning of base date
            
            for (let d = 0; d < schedule.days; d++) {
                for (let t = 0; t < 24; t++) {
                    if (selectedCells[d][t]) {
                        // Use exact timestamp offsets to avoid timezone drift
                        const start = new Date(startDate.getTime() + (d * 24 + t) * 60 * 60 * 1000);
                        const end = new Date(start.getTime() + 60 * 60 * 1000);
                        
                        timeBlocks.push({
                            start: start.toISOString(),
                            end: end.toISOString()
                        });
                    }
                }
            }

			await fetchApi(`/api/schedules/${scheduleId}/availability/me`, {
				method: 'PUT',
				body: JSON.stringify({ blocks: timeBlocks })
			});

			alert('Availability saved successfully!');
			window.history.back();
		} catch (err) {
			console.error(err);
			if (!(err instanceof ApiError && err.status === 401)) {
				alert('An error occurred while submitting availability.');
			}
		} finally {
			isSubmitting = false;
		}
	}

    onMount(async () => {
        try {
            // Fetch schedule details first
            const scheduleData = await fetchJson<any>(`/api/groups/${$page.params.groupId || 'unknown'}/schedules`).then(list => list.find((s: any) => s.id === scheduleId));
            if (scheduleData) {
                schedule = {
                    ...schedule,
                    title: scheduleData.title,
                    start: scheduleData.start
                };
            }

            // Fetch existing availability
            const myAvail = await fetchJson<any>(`/api/schedules/${scheduleId}/availability/me`);
            if (myAvail && myAvail.blocks) {
                const baseDate = new Date(schedule.start);
                baseDate.setHours(0, 0, 0, 0);

                myAvail.blocks.forEach((block: any) => {
                    const blockDate = new Date(block.start);
                    const diffMs = blockDate.getTime() - baseDate.getTime();
                    const diffHoursTotal = Math.floor(diffMs / (1000 * 60 * 60));
                    
                    const d = Math.floor(diffHoursTotal / 24);
                    const t = diffHoursTotal % 24;

                    if (d >= 0 && d < 8 && t >= 0 && t < 24) {
                        selectedCells[d][t] = true;
                    }
                });
                selectedCells = [...selectedCells];
            }
        } catch (err) {
            console.error('Failed to load existing availability:', err);
        }
    });
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
                    <p class="text-sm opacity-80 mb-4 font-neo text-center">Click and drag to select or deselect the times you are available to raid.</p>
					
                    <!-- Desktop Heatmap (Dates = Rows) -->
                    <div class="hidden md:block overflow-x-auto bg-base-200 p-4 rounded-lg">
                        <table class="table table-xs w-full text-center border-collapse">
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
                                                class="border border-base-300 transition-colors cursor-crosshair p-0 
                                                {isInDragBox(d, t) ? (dragState ? 'bg-primary/70' : 'bg-base-300') : (selectedCells[d][t] ? 'bg-primary' : 'bg-base-100 hover:bg-base-300')}"
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
                        <table class="table table-xs w-full text-center border-collapse">
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
                                                class="border border-base-300 transition-colors cursor-crosshair p-0 
                                                {isInDragBox(d, t) ? (dragState ? 'bg-primary/70' : 'bg-base-300') : (selectedCells[d][t] ? 'bg-primary' : 'bg-base-100 hover:bg-base-300')}"
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

					<div class="flex justify-between items-center mt-8">
                        <button class="btn btn-outline btn-error" onclick={() => {
                            selectedCells = Array(schedule.days).fill(null).map(() => Array(24).fill(false));
                        }}>
                            Clear All
                        </button>
                        <div class="space-x-2">
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
