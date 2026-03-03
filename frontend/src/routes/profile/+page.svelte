<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';

	let token = $state('');
	let user = $state<any>(null);
	let isLoading = $state(true);
	let error = $state('');
    let newNickname = $state('');
    let isSaving = $state(false);

	const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

	onMount(async () => {
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
					logout();
					return;
				}
				throw new Error('Failed to fetch user profile');
			}

			user = await response.json();
            newNickname = user.username;
		} catch (err) {
			console.error(err);
			error = 'Failed to load user data.';
		} finally {
			isLoading = false;
		}
	});

	function logout() {
		localStorage.removeItem('ark_token');
		window.location.href = `${base}/`;
	}

    async function updateNickname() {
        if (!newNickname || newNickname === user.username) return;
        
        isSaving = true;
        // Mocking backend update for now as requested (Frontend work only)
        setTimeout(() => {
            user.username = newNickname;
            isSaving = false;
            alert('Nickname updated! (Note: This is frontend-only until backend is ready)');
        }, 500);
    }

    function deleteAccount() {
        if (confirm('Are you absolutely sure you want to delete your account? This action is irreversible.')) {
            alert('Account deletion requested. (Frontend-only mock)');
            logout();
        }
    }

	function getAvatarUrl(discordId: string, avatarHash: string) {
		if (!avatarHash) return `https://cdn.discordapp.com/embed/avatars/${parseInt(discordId) % 5}.png`;
		return `https://cdn.discordapp.com/avatars/${discordId}/${avatarHash}.png`;
	}
</script>

<svelte:head>
	<title>Profile - Ark Resolver</title>
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
		<div class="container mx-auto max-w-2xl">
			
            <div class="flex items-center gap-4 mb-8">
                <a href="{base}/dashboard" class="btn btn-ghost btn-circle">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" /></svg>
                </a>
                <h1 class="text-3xl font-bold text-ubuntu">User Profile</h1>
            </div>

			{#if isLoading}
				<div class="flex justify-center items-center h-64">
					<span class="loading loading-spinner loading-lg text-primary"></span>
				</div>
			{:else if error}
				<div class="alert alert-error shadow-lg">
					<span>{error}</span>
					<button class="btn btn-sm btn-ghost" onclick={logout}>Login Again</button>
				</div>
			{:else}
				<div class="space-y-6">
					<!-- Profile Card -->
					<div class="card bg-base-100 shadow-xl overflow-hidden">
                        <div class="h-32 bg-gradient-to-r from-primary via-accent to-secondary"></div>
                        <div class="card-body -mt-16 items-center text-center">
                            <div class="avatar ring-offset-base-100 ring-offset-4 ring-primary ring-4 rounded-full">
                                <div class="w-32 rounded-full bg-neutral shadow-2xl">
                                    <img src={getAvatarUrl(user.discordId, user.avatar)} alt={user.username} />
                                </div>
                            </div>
                            <div class="mt-4">
                                <h2 class="text-3xl font-bold">{user.username}</h2>
                                <p class="opacity-60">Connected with Discord</p>
                            </div>
                        </div>
                    </div>

                    <!-- Settings Sections -->
                    <div class="card bg-base-100 shadow-xl">
                        <div class="card-body">
                            <h3 class="text-xl font-bold mb-4 text-ubuntu">Account Settings</h3>
                            
                            <div class="form-control w-full">
                                <label class="label" for="nickname">
                                    <span class="label-text font-semibold">Custom Nickname</span>
                                </label>
                                <div class="flex flex-col sm:flex-row gap-2">
                                    <input 
                                        id="nickname"
                                        type="text" 
                                        placeholder="Enter new nickname" 
                                        class="input input-bordered flex-1 font-nanum" 
                                        bind:value={newNickname}
                                        onkeydown={(e) => e.key === 'Enter' && updateNickname()}
                                    />
                                    <button 
                                        class="btn btn-primary" 
                                        disabled={isSaving || newNickname === user.username}
                                        onclick={updateNickname}
                                    >
                                        {#if isSaving}
                                            <span class="loading loading-spinner"></span>
                                        {/if}
                                        Save Changes
                                    </button>
                                </div>
                                <label class="label" for="nickname">
                                    <span class="label-text-alt opacity-60">This will change how you appear to others in parties.</span>
                                </label>
                            </div>

                            <div class="divider"></div>

                            <h3 class="text-xl font-bold mb-4 text-ubuntu">Discord Information</h3>
                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
                                <div class="bg-base-200 p-3 rounded-lg">
                                    <span class="block opacity-60 uppercase text-xs font-bold mb-1">Discord Username</span>
                                    <span class="font-mono">{user.username}</span>
                                </div>
                                <div class="bg-base-200 p-3 rounded-lg">
                                    <span class="block opacity-60 uppercase text-xs font-bold mb-1">Discord User ID</span>
                                    <span class="font-mono">{user.discordId}</span>
                                </div>
                            </div>

                            <div class="divider"></div>

                            <div class="bg-error/10 border border-error/20 p-6 rounded-lg mt-4">
                                <h4 class="text-error font-bold text-lg mb-2 text-ubuntu">Danger Zone</h4>
                                <p class="text-sm opacity-80 mb-4">Deleting your account will remove all your data and dissociate your Discord account from Ark Resolver.</p>
                                <button class="btn btn-error btn-outline w-full sm:w-auto" onclick={deleteAccount}>
                                    Delete My Account
                                </button>
                            </div>
                        </div>
                    </div>
				</div>
			{/if}
		</div>
	</main>
</div>
