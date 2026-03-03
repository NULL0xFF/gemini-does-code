<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';

	let token = $state('');
	let user = $state<any>(null);
	let isLoading = $state(true);
	let error = $state('');
	let showSuccessToast = $state(false);

	const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

	onMount(async () => {
		const params = new URLSearchParams(window.location.search);
		if (params.get('login') === 'success') {
			showSuccessToast = true;
			// Clean up URL
			window.history.replaceState({}, '', window.location.pathname);
			setTimeout(() => { showSuccessToast = false; }, 5000);
		}

		token = localStorage.getItem('ark_token') || '';
		if (!token) {
			window.location.href = `${base}/login`;
			return;
		}

		try {
			const response = await fetch(`${apiUrl}/api/users/me`, {
				headers: {
					'Authorization': `Bearer ${token}`
				}
			});

			if (!response.ok) {
				if (response.status === 401) {
					logout('session_invalid');
					return;
				}
				throw new Error('Failed to fetch user profile');
			}

			user = await response.json();
		} catch (err) {
			console.error(err);
			error = 'Failed to load user data. Please try logging in again.';
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
						<span>Welcome back! You have successfully logged in.</span>
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
					<!-- User Profile Sidebar -->
					<div class="card w-full md:w-80 bg-base-100 shadow-xl overflow-hidden">
						<div class="h-24 bg-primary"></div>
						<div class="px-6 pb-6 text-center">
							<div class="avatar -mt-12 mb-4 ring-offset-base-100 ring-offset-2 ring-primary ring">
								<div class="w-24 rounded-full bg-neutral">
									<img src={getAvatarUrl(user.discordId, user.avatar)} alt={user.username} />
								</div>
							</div>
							<h2 class="text-2xl font-bold">{user.username}</h2>
							<p class="text-sm opacity-60">ID: {user.discordId}</p>
							
							<div class="divider"></div>
							
							<div class="flex flex-col gap-2">
								<button class="btn btn-outline btn-sm">Edit Profile</button>
								<button class="btn btn-outline btn-sm">Discord Settings</button>
							</div>
						</div>
					</div>

					<!-- Main Content -->
					<div class="flex-1 w-full space-y-6">
						<div class="card bg-base-100 shadow-xl">
							<div class="card-body">
								<h2 class="card-title text-2xl">Dashboard Overview</h2>
								<p class="opacity-80 mt-2">Welcome to your command center. Coordinate raids, manage your parties, and keep track of your team's status.</p>
							</div>
						</div>

						<div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
							<div class="card bg-base-100 shadow-xl border-t-4 border-info">
								<div class="card-body">
									<h2 class="card-title">My Parties</h2>
									<p class="opacity-70">Assemble your group and start your journey.</p>
									<div class="card-actions justify-end mt-4">
										<button class="btn btn-primary">Create Party</button>
									</div>
								</div>
							</div>
							<div class="card bg-base-100 shadow-xl border-t-4 border-secondary">
								<div class="card-body">
									<h2 class="card-title">Join a Raid</h2>
									<p class="opacity-70">Find active parties looking for your specific role.</p>
									<div class="card-actions justify-end mt-4">
										<button class="btn btn-secondary">Browse LFG</button>
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
