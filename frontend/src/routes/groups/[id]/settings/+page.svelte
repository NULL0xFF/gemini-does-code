<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import { page } from '$app/stores';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { fetchApi, fetchJson, ApiError } from '$lib/api';

	let groupId = $derived($page.params.id);
	let groupName = $state('');
	let description = $state('');
	let isLoading = $state(true);
	let isSaving = $state(false);
	let error = $state('');

	function logout() {
		localStorage.removeItem('ark_token');
		window.location.href = `${base}/`;
	}

	onMount(async () => {
		try {
			const group = await fetchJson<any>(`/api/groups/${groupId}`);
			groupName = group.name;
			description = group.description || '';
		} catch (err) {
			console.error(err);
			if (!(err instanceof ApiError && err.status === 401)) {
				error = 'Failed to load group settings.';
			}
		} finally {
			isLoading = false;
		}
	});

	async function handleSubmit() {
		if (!groupName.trim()) return;

		isSaving = true;
		try {
			await fetchApi(`/api/groups/${groupId}`, {
				method: 'PUT',
				body: JSON.stringify({
					name: groupName,
					description: description
				})
			});

			alert('Group settings updated successfully!');
			window.location.href = `${base}/groups/${groupId}`;
		} catch (err) {
			console.error(err);
			alert('Failed to update group settings.');
		} finally {
			isSaving = false;
		}
	}

	async function deleteGroup() {
		if (confirm('Are you absolutely sure you want to delete this group? This action is irreversible.')) {
			try {
				await fetchApi(`/api/groups/${groupId}`, { method: 'DELETE' });
				alert('Group deleted successfully.');
				window.location.href = `${base}/dashboard`;
			} catch (err) {
				console.error(err);
				alert('Failed to delete group.');
			}
		}
	}
</script>

<svelte:head>
	<title>Group Settings - Ark Resolver</title>
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
		<div class="container mx-auto max-w-2xl">
			<div class="flex items-center gap-4 mb-8">
				<a href="{base}/groups/{groupId}" class="btn btn-ghost btn-circle" aria-label="Back to Group">
					<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" /></svg>
				</a>
				<h1 class="text-3xl font-bold text-ubuntu">Group Settings</h1>
			</div>

			{#if isLoading}
				<div class="flex justify-center items-center h-64">
					<span class="loading loading-spinner loading-lg text-primary"></span>
				</div>
			{:else if error}
				<div class="alert alert-error shadow-lg">
					<span>{error}</span>
					<a href="{base}/groups/{groupId}" class="btn btn-sm btn-ghost">Return</a>
				</div>
			{:else}
				<div class="card bg-base-100 shadow-xl">
					<div class="card-body">
						<div class="space-y-6">
							<div>
								<label for="group-name" class="block text-sm font-bold text-ubuntu mb-2">Group Name</label>
								<input 
									id="group-name"
									type="text" 
									class="input input-bordered w-full font-neo focus:ring-primary" 
									bind:value={groupName}
									onkeydown={(e) => e.key === 'Enter' && handleSubmit()}
								/>
							</div>

							<div>
								<label for="description" class="block text-sm font-bold text-ubuntu mb-2">Description</label>
								<textarea 
									id="description"
									class="textarea textarea-bordered h-32 w-full font-neo focus:ring-primary" 
									bind:value={description}
								></textarea>
							</div>
						</div>

						<div class="card-actions justify-end mt-8">
							<a href="{base}/groups/{groupId}" class="btn btn-ghost">Cancel</a>
							<button 
								class="btn btn-primary px-8" 
								disabled={isSaving || !groupName.trim()}
								onclick={handleSubmit}
							>
								{#if isSaving}
									<span class="loading loading-spinner"></span>
								{/if}
								Save Changes
							</button>
						</div>
					</div>
				</div>

				<div class="card bg-base-100 shadow-xl mt-8 border-t-4 border-error">
					<div class="card-body">
						<h3 class="text-error font-bold text-lg mb-2 text-ubuntu text-ubuntu">Danger Zone</h3>
						<p class="text-sm opacity-80 mb-4">Deleting this group will remove all schedules, parties, and memberships permanently.</p>
						<button class="btn btn-error btn-outline w-full sm:w-auto" onclick={deleteGroup}>
							Delete This Group
						</button>
					</div>
				</div>
			{/if}
		</div>
	</main>
</div>
