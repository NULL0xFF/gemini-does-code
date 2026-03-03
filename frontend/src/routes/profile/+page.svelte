<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';

	let token = $state('');
	let user = $state<any>(null);
	let isLoading = $state(true);
	let error = $state('');
    let customNickname = $state('');
    let isSaving = $state(false);
    let isSyncing = $state(false);

	const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

	onMount(async () => {
		token = localStorage.getItem('ark_token') || '';
		if (!token) {
			window.location.href = `${base}/login`;
			return;
		}

		await fetchUserData();
	});

    async function fetchUserData() {
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
            // Fallback logic: if nickname is empty/null, use Discord username
            customNickname = user.nickname || '';
		} catch (err) {
			console.error(err);
			error = 'Failed to load user data.';
		} finally {
			isLoading = false;
		}
    }

	function logout(reason?: string) {
		localStorage.removeItem('ark_token');
        const reasonStr = (typeof reason === 'string') ? reason : '';
		const url = reasonStr ? `${base}/login?error=${reasonStr}` : `${base}/`;
		window.location.href = url;
	}

    async function updateNickname() {
        // Validation: only save if it's different from current nickname
        if (customNickname === user.nickname) return;
        
        isSaving = true;
        try {
            const response = await fetch(`${apiUrl}/api/users/me/nickname`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ nickname: customNickname })
            });

            if (!response.ok) throw new Error('Failed to update nickname');

            user.nickname = customNickname;
            alert('Nickname updated successfully!');
        } catch (err) {
            console.error(err);
            alert('Failed to update nickname.');
        } finally {
            isSaving = false;
        }
    }

    async function syncDiscord() {
        isSyncing = true;
        try {
            const response = await fetch(`${apiUrl}/api/users/me/sync`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) throw new Error('Failed to sync with Discord');

            await fetchUserData();
            alert('Profile synced with Discord!');
        } catch (err) {
            console.error(err);
            alert('Failed to sync with Discord.');
        } finally {
            isSyncing = false;
        }
    }

    async function deleteAccount() {
        if (confirm('Are you absolutely sure you want to delete your account? This action is irreversible.')) {
            try {
                const response = await fetch(`${apiUrl}/api/users/me`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!response.ok) throw new Error('Failed to delete account');

                alert('Account deleted successfully. We hope to see you again!');
                logout();
            } catch (err) {
                console.error(err);
                alert('Failed to delete account.');
            }
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
                <a href="{base}/dashboard" class="btn btn-ghost btn-circle" aria-label="Back to Dashboard">
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
                                <h2 class="text-3xl font-bold">{customNickname || user.username}</h2>
                                <p class="opacity-60 flex items-center justify-center gap-2">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 127.14 96.36"><path d="M107.7,8.07A105.15,105.15,0,0,0,81.47,0a72.06,72.06,0,0,0-3.36,6.83A97.68,97.68,0,0,0,49,6.83,72.37,72.37,0,0,0,45.64,0,105.89,105.89,0,0,0,19.39,8.09C2.79,32.65-1.71,56.6.54,80.21h0A105.73,105.73,0,0,0,32.71,96.36,77.7,77.7,0,0,0,39.6,85.25a68.42,68.42,0,0,1-10.85-5.18c.91-.66,1.8-1.34,2.66-2a75.57,75.57,0,0,0,64.32,0c.87.71,1.76,1.39,2.66,2a68.68,68.68,0,0,1-10.87,5.19,77,77,0,0,0,6.89,11.1A105.25,105.25,0,0,0,126.6,80.22h0C129.24,52.84,122.09,29.11,107.7,8.07ZM42.45,65.69C36.18,65.69,31,60,31,53s5-12.74,11.43-12.74S54,46,53.89,53C53.89,60,48.84,65.69,42.45,65.69Zm42.24,0C78.41,65.69,73.31,60,73.31,53s5-12.74,11.43-12.74S96.16,46,96.06,53C96.06,60,91.08,65.69,84.69,65.69Z"/></svg>
                                    {user.username}
                                </p>
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
                                        placeholder={user.username} 
                                        class="input input-bordered flex-1 font-nanum" 
                                        bind:value={customNickname}
                                        onkeydown={(e) => e.key === 'Enter' && updateNickname()}
                                    />
                                    <button 
                                        class="btn btn-primary" 
                                        disabled={isSaving || customNickname === (user.nickname || '')}
                                        onclick={updateNickname}
                                    >
                                        {#if isSaving}
                                            <span class="loading loading-spinner"></span>
                                        {/if}
                                        Save Changes
                                    </button>
                                </div>
                                <label class="label" for="nickname">
                                    <span class="label-text-alt opacity-60">This changes your display name in Ark Resolver. Leave empty to use Discord username.</span>
                                </label>
                            </div>

                            <div class="divider"></div>

                            <div class="flex justify-between items-center mb-4">
                                <h3 class="text-xl font-bold text-ubuntu">Discord Information</h3>
                                <button 
                                    class="btn btn-ghost btn-sm gap-2" 
                                    onclick={syncDiscord}
                                    disabled={isSyncing}
                                >
                                    {#if isSyncing}
                                        <span class="loading loading-spinner loading-xs"></span>
                                    {:else}
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0 3.181 3.183a8.25 8.25 0 0 0 13.803-3.7M4.031 9.865a8.25 8.25 0 0 1 13.803-3.7l3.181 3.182m0-4.991v4.99" /></svg>
                                    {/if}
                                    Sync with Discord
                                </button>
                            </div>
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
