<script lang="ts">
	import { base } from '$app/paths';
	import { page } from '$app/stores';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { fetchApi, fetchJson, ApiError } from '$lib/api';

	let groupId = $derived($page.params.id);
	
	// Form state
	let maxUsage = $state(1);
	let expirationDays = $state(7);
	let isGenerating = $state(false);

	// List state
	let activeCodes = $state([
		{ code: 'ARK-W8BX-P2Q9', max: 5, used: 2, expires: '2026-03-10' },
		{ code: 'ARK-K9L1-M4N5', max: 1, used: 0, expires: '2026-03-05' }
	]);

	function logout() {
		localStorage.removeItem('ark_token');
		window.location.href = `${base}/`;
	}

	async function generateCode() {
		isGenerating = true;
		try {
			// Mock backend call
			await new Promise(r => setTimeout(r, 500));
			
			const newCode = 'ARK-' + Math.random().toString(36).substring(2, 6).toUpperCase() + '-' + Math.random().toString(36).substring(2, 6).toUpperCase();
			
			const d = new Date();
			d.setDate(d.getDate() + expirationDays);
			
			activeCodes = [{
				code: newCode,
				max: maxUsage,
				used: 0,
				expires: d.toISOString().split('T')[0]
			}, ...activeCodes];

			alert(`Invite code generated: ${newCode}`);
			
			// Reset form
			maxUsage = 1;
			expirationDays = 7;
		} catch (err) {
			console.error(err);
			alert('Failed to generate invite code.');
		} finally {
			isGenerating = false;
		}
	}

	async function revokeCode(codeToRevoke: string) {
		if (confirm(`Are you sure you want to revoke code ${codeToRevoke}?`)) {
			// Mock backend call
			activeCodes = activeCodes.filter(c => c.code !== codeToRevoke);
		}
	}

	function copyToClipboard(text: string) {
		navigator.clipboard.writeText(text).then(() => {
			// Simple visual feedback could be added here
			alert('Copied to clipboard!');
		});
	}
</script>

<svelte:head>
	<title>Manage Invites - Ark Resolver</title>
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
				<a href="{base}/groups/{groupId}" class="btn btn-ghost btn-circle" aria-label="Back to Group">
					<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" /></svg>
				</a>
				<div>
					<h1 class="text-3xl font-bold text-ubuntu">Manage Invites</h1>
					<p class="opacity-70 font-neo text-sm">Control who can join your group.</p>
				</div>
			</div>

			<div class="grid grid-cols-1 md:grid-cols-3 gap-8">
				<!-- Generate Form -->
				<div class="md:col-span-1">
					<div class="card bg-base-100 shadow-xl border-t-4 border-primary">
						<div class="card-body">
							<h3 class="card-title text-lg text-ubuntu mb-4">Generate New Code</h3>
							
							<div class="space-y-4">
								<div>
									<label for="max-uses" class="block text-sm font-bold text-ubuntu mb-2">Max Uses</label>
									<input 
										id="max-uses"
										type="number" 
										min="1"
										max="100"
										class="input input-bordered w-full font-mono text-center" 
										bind:value={maxUsage}
									/>
									<span class="text-xs opacity-60 mt-1 block">How many people can use this code.</span>
								</div>

								<div>
									<label for="expiration" class="block text-sm font-bold text-ubuntu mb-2">Valid For (Days)</label>
									<input 
										id="expiration"
										type="number" 
										min="1"
										max="30"
										class="input input-bordered w-full font-mono text-center" 
										bind:value={expirationDays}
									/>
								</div>

								<button 
									class="btn btn-primary w-full mt-4" 
									disabled={isGenerating}
									onclick={generateCode}
								>
									{#if isGenerating}
										<span class="loading loading-spinner"></span>
									{/if}
									Create Code
								</button>
							</div>
						</div>
					</div>
				</div>

				<!-- Active Codes List -->
				<div class="md:col-span-2">
					<div class="card bg-base-100 shadow-xl">
						<div class="card-body p-0">
							<h3 class="font-bold text-lg text-ubuntu p-6 pb-2">Active Invite Codes</h3>
							<div class="overflow-x-auto">
								<table class="table w-full">
									<thead>
										<tr class="bg-base-200">
											<th>Code</th>
											<th class="text-center">Usage</th>
											<th>Expires</th>
											<th class="text-right">Actions</th>
										</tr>
									</thead>
									<tbody>
										{#each activeCodes as invite}
											<tr class="hover:bg-base-200/50 transition-colors">
												<td class="font-mono font-bold tracking-wider text-primary">
													<button 
														class="hover:underline flex items-center gap-2"
														onclick={() => copyToClipboard(invite.code)}
														title="Click to copy"
													>
														{invite.code}
														<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4 opacity-50"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 17.25v3.375c0 .621-.504 1.125-1.125 1.125h-9.75a1.125 1.125 0 01-1.125-1.125V7.875c0-.621.504-1.125 1.125-1.125H6.75a9.06 9.06 0 011.5.124m7.5 10.376h3.375c.621 0 1.125-.504 1.125-1.125V11.25c0-4.46-3.243-8.161-7.5-8.876a9.06 9.06 0 00-1.5-.124H9.375c-.621 0-1.125.504-1.125 1.125v3.5m7.5 10.375H9.375a1.125 1.125 0 01-1.125-1.125v-9.25m12 6.625v-1.875a3.375 3.375 0 00-3.375-3.375h-1.5a1.125 1.125 0 01-1.125-1.125v-1.5a3.375 3.375 0 00-3.375-3.375H9.75" /></svg>
													</button>
												</td>
												<td class="text-center font-mono text-sm">
													{invite.used} / {invite.max}
													<progress class="progress progress-primary w-16 ml-2" value={invite.used} max={invite.max}></progress>
												</td>
												<td class="text-sm font-neo opacity-80">{invite.expires}</td>
												<td class="text-right">
													<button 
														class="btn btn-ghost btn-xs text-error hover:bg-error/20"
														onclick={() => revokeCode(invite.code)}
													>
														Revoke
													</button>
												</td>
											</tr>
										{/each}
										{#if activeCodes.length === 0}
											<tr>
												<td colspan="4" class="text-center py-8 opacity-50 italic">No active invite codes.</td>
											</tr>
										{/if}
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</main>
</div>
