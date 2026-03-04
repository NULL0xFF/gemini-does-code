<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';
	import { fetchJson, ApiError } from '$lib/api';

	let token = $state('');
	let user = $state<any>(null);
	let groups = $state<any[]>([]);
	let isLoading = $state(true);
	let error = $state('');
	let showSuccessToast = $state(false);
	let successMessage = $state('');

	onMount(async () => {
		const params = new URLSearchParams(window.location.search);
		if (params.get('login') === 'success') {
			successMessage = 'Welcome back! You have successfully logged in.';
			showSuccessToast = true;
			window.history.replaceState({}, '', window.location.pathname);
			setTimeout(() => { showSuccessToast = false; }, 5000);
		} else if (params.get('group_created') === 'success') {
			successMessage = 'Your new group has been created successfully!';
			showSuccessToast = true;
			window.history.replaceState({}, '', window.location.pathname);
			setTimeout(() => { showSuccessToast = false; }, 5000);
		}

		token = localStorage.getItem('ark_token') || '';
		if (!token) {
			window.location.href = `${base}/login`;
			return;
		}

		try {
			const [userData, userGroups] = await Promise.all([
				fetchJson<any>('/api/users/me'),
				fetchJson<any[]>('/api/groups')
			]);
			user = userData;
			groups = userGroups;
		} catch (err) {
			console.error(err);
			// 401 is handled globally by fetchJson
			if (!(err instanceof ApiError && err.status === 401)) {
				error = 'Failed to load dashboard data. Please try logging in again.';
			}
		} finally {
			isLoading = false;
		}
	});

	function logout(reason?: any) {
		localStorage.removeItem('ark_token');
		const reasonStr = (typeof reason === 'string') ? reason : '';
		const url = reasonStr ? `${base}/login?error=${reasonStr}` : `${base}/`;
		window.location.href = url;
	}

	function getAvatarUrl(discordId: string, avatarHash: string) {
		if (!avatarHash) return `https://cdn.discordapp.com/embed/avatars/${parseInt(discordId) % 5}.png`;
		return `https://cdn.discordapp.com/avatars/${discordId}/${avatarHash}.png`;
	}
</script>

<svelte:head>
	<title>Dashboard - Ark Resolver</title>
</svelte:head>

<div class="min-h-screen bg-base-200 flex flex-col">
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
		<div class="container mx-auto max-w-5xl">
			
			{#if showSuccessToast}
				<div class="toast toast-top toast-center z-[100]">
					<div class="alert alert-success shadow-lg text-white">
						<svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
						<span>{successMessage}</span>
						<button class="btn btn-sm btn-ghost" onclick={() => showSuccessToast = false}>✕</button>
					</div>
				</div>
			{/if}

			{#if isLoading}
				<div class="flex justify-center items-center h-64">
					<span class="loading loading-spinner loading-lg text-primary"></span>
				</div>
			{:else if error}
				<div class="alert alert-error shadow-lg">
					<svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
					<span>{error}</span>
					<button class="btn btn-sm btn-ghost" onclick={logout}>Login Again</button>
				</div>
			{:else}
				<div class="flex flex-col md:flex-row gap-8 items-start">
					<div class="card w-full md:w-80 bg-base-100 shadow-xl overflow-hidden">
						<div class="h-24 bg-gradient-to-r from-primary to-secondary"></div>
						<div class="px-6 pb-6 text-center">
							<div class="avatar -mt-12 mb-4 ring-offset-base-100 ring-offset-4 ring-primary ring-4 rounded-full">
								<div class="w-24 rounded-full bg-neutral">
									<img src={getAvatarUrl(user.discordId, user.avatar)} alt={user.username} />
								</div>
							</div>
							<h2 class="text-2xl font-bold">{user.nickname || user.username}</h2>
							<p class="text-sm opacity-60">ID: {user.discordId}</p>
							
							<div class="divider"></div>
							
							<div class="flex flex-col gap-2">
								<a href="{base}/profile" class="btn btn-primary btn-outline btn-sm">Edit Profile</a>
							</div>
						</div>
					</div>

					<div class="flex-1 w-full space-y-6">
						<div class="card bg-base-100 shadow-xl">
							<div class="card-body">
								<h2 class="card-title text-2xl text-ubuntu">Welcome to Ark Resolver, {user.nickname || user.username}!</h2>
								<p class="opacity-80 mt-2">Welcome to your command center. Coordinate raids, manage your parties, and keep track of your team's status.</p>
							</div>
						</div>

						<div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
							<div class="card bg-base-100 shadow-xl border-t-4 border-info">
								<div class="card-body">
									<div class="flex justify-between items-center mb-2">
										<h2 class="card-title text-ubuntu font-bold">My Groups</h2>
										<a href="{base}/groups/create" class="btn btn-primary btn-sm">New Group</a>
									</div>
									
									{#if groups.length === 0}
										<p class="opacity-70 text-sm">You are not a member of any groups yet.</p>
									{:else}
										<div class="space-y-3 mt-2">
											{#each groups as group}
												<a href="{base}/groups/{group.id}" class="p-3 bg-base-200 rounded-lg hover:bg-base-300 transition-colors cursor-pointer group block">
													<div class="flex justify-between items-center">
														<span class="font-bold text-sm text-ubuntu">{group.name}</span>
														<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4 opacity-0 group-hover:opacity-100 transition-opacity"><path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" /></svg>
													</div>
													{#if group.description}
														<p class="text-xs opacity-60 truncate mt-1">{group.description}</p>
													{/if}
												</a>
											{/each}
										</div>
									{/if}
								</div>
							</div>
							<div class="card bg-base-100 shadow-xl border-t-4 border-secondary">
								<div class="card-body">
									<h2 class="card-title text-ubuntu font-bold">Join a Group</h2>
									<p class="opacity-70 text-sm">Use an invite code to join an existing group roster.</p>
									<div class="card-actions justify-end mt-4">
										<a href="{base}/groups/join" class="btn btn-secondary btn-sm md:btn-md">Enter Code</a>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			{/if}
		</div>
	</main>
</div>
