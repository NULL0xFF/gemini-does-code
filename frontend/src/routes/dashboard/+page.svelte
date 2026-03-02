<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import ThemeToggle from '$lib/components/ThemeToggle.svelte';

	let token = $state('');

	onMount(() => {
		token = localStorage.getItem('ark_token') || '';
		if (!token) {
			window.location.href = `${base}/login`;
		}
	});

	function logout() {
		localStorage.removeItem('ark_token');
		window.location.href = `${base}/`;
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
			<button class="btn btn-outline btn-error" onclick={logout}>Logout</button>
		</div>
	</div>

	<main class="flex-1 p-8">
		<div class="container mx-auto max-w-4xl">
			<h1 class="text-4xl font-bold mb-8">Dashboard</h1>
			
			<div class="card bg-base-100 shadow-xl mb-8">
				<div class="card-body">
					<h2 class="card-title text-2xl text-success">Authentication Successful!</h2>
					<p>You have successfully logged in via Discord. Here is your raw JWT Token:</p>
					
					<div class="mockup-code mt-4 bg-neutral text-neutral-content">
						<pre data-prefix=">"><code>{token}</code></pre>
					</div>
				</div>
			</div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="card bg-base-100 shadow-xl border-t-4 border-info">
                    <div class="card-body">
                        <h2 class="card-title">My Parties</h2>
                        <p class="opacity-70">You are not currently in any parties.</p>
                        <div class="card-actions justify-end mt-4">
                            <button class="btn btn-primary" disabled>Create Party</button>
                        </div>
                    </div>
                </div>
                <div class="card bg-base-100 shadow-xl border-t-4 border-secondary">
                    <div class="card-body">
                        <h2 class="card-title">Looking For Group</h2>
                        <p class="opacity-70">No active LFG postings found.</p>
                        <div class="card-actions justify-end mt-4">
                            <button class="btn btn-secondary" disabled>Browse LFG</button>
                        </div>
                    </div>
                </div>
            </div>
		</div>
	</main>
</div>
