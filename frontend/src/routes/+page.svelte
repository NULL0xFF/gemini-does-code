<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';

	let backendMessage = 'Checking connection...';
	const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

	onMount(async () => {
		try {
			const response = await fetch(`${apiUrl}/api/hello`);
			if (!response.ok) throw new Error('Network response was not ok');
			const data = await response.json();
			backendMessage = data.message;
		} catch (error) {
			console.error('Failed to fetch:', error);
			backendMessage = 'Failed to load data from backend.';
		}
	});
</script>

<div class="min-h-screen bg-base-200 flex flex-col">
	<!-- Navbar -->
	<div class="navbar bg-base-100 shadow-sm sticky top-0 z-50">
		<div class="navbar-start">
			<div class="dropdown">
				<div tabindex="0" role="button" class="btn btn-ghost lg:hidden">
					<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h8m-8 6h16" /></svg>
				</div>
				<ul tabindex="0" class="menu menu-sm dropdown-content mt-3 z-[1] p-2 shadow bg-base-100 rounded-box w-52">
					<li><a href="#features">Features</a></li>
					<li><a href="#status">Status</a></li>
				</ul>
			</div>
			<a class="btn btn-ghost text-xl font-bold" href="{base}/">Ark Resolver</a>
		</div>
		<div class="navbar-center hidden lg:flex">
			<ul class="menu menu-horizontal px-1 font-semibold">
				<li><a href="#features">Features</a></li>
				<li><a href="#status">Status</a></li>
			</ul>
		</div>
		<div class="navbar-end">
			<button class="btn btn-primary">Get Started</button>
		</div>
	</div>

	<!-- Hero Section -->
	<div class="hero flex-1 bg-base-200">
		<div class="hero-content text-center py-20">
			<div class="max-w-2xl">
				<h1 class="text-5xl font-bold mb-6">Group Party Management System</h1>
				<p class="text-xl mb-8 opacity-80">
					Streamline your party forming process, efficiently manage groups, and ensure everyone is aligned for your next big raid. Ark Resolver is your ultimate party orchestration tool.
				</p>
				<div class="flex justify-center gap-4">
					<button class="btn btn-primary btn-lg">Create a Party</button>
					<button class="btn btn-outline btn-lg">Learn More</button>
				</div>
			</div>
		</div>
	</div>

	<!-- Features Section -->
	<div id="features" class="py-20 bg-base-100">
		<div class="container mx-auto px-4">
			<h2 class="text-4xl font-bold text-center mb-16">Why Choose Ark Resolver?</h2>
			
			<div class="grid grid-cols-1 md:grid-cols-3 gap-8">
				<!-- Card 1 -->
				<div class="card bg-base-200 shadow-xl hover:-translate-y-2 transition-transform duration-300">
					<div class="card-body items-center text-center">
						<div class="text-primary mb-4">
							<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-12 h-12">
								<path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 0 0 2.625.372 9.337 9.337 0 0 0 4.121-.952 4.125 4.125 0 0 0-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 0 1 8.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0 1 11.964-3.07M12 6.375a3.375 3.375 0 1 1-6.75 0 3.375 3.375 0 0 1 6.75 0Zm8.25 2.25a2.625 2.625 0 1 1-5.25 0 2.625 2.625 0 0 1 5.25 0Z" />
							</svg>
						</div>
						<h3 class="card-title text-xl mb-2">Efficient Grouping</h3>
						<p class="opacity-75">Quickly assemble rosters based on roles, requirements, and availability. Say goodbye to spreadsheet chaos.</p>
					</div>
				</div>

				<!-- Card 2 -->
				<div class="card bg-base-200 shadow-xl hover:-translate-y-2 transition-transform duration-300">
					<div class="card-body items-center text-center">
						<div class="text-secondary mb-4">
							<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-12 h-12">
								<path stroke-linecap="round" stroke-linejoin="round" d="M3.75 3v11.25A2.25 2.25 0 0 0 6 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0 1 18 16.5h-2.25m-7.5 0h7.5m-7.5 0-1 3m8.5-3 1 3m0 0 .5 1.5m-.5-1.5h-9.5m0 0-.5 1.5m.75-9 3-3 2.148 2.148A12.061 12.061 0 0 1 16.5 7.605" />
							</svg>
						</div>
						<h3 class="card-title text-xl mb-2">Real-time Analytics</h3>
						<p class="opacity-75">Get instant insights into group compositions, missing roles, and readiness status across all ongoing parties.</p>
					</div>
				</div>

				<!-- Card 3 -->
				<div class="card bg-base-200 shadow-xl hover:-translate-y-2 transition-transform duration-300">
					<div class="card-body items-center text-center">
						<div class="text-accent mb-4">
							<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-12 h-12">
								<path stroke-linecap="round" stroke-linejoin="round" d="M14.25 9.75 16.5 12l-2.25 2.25m-4.5 0L7.5 12l2.25-2.25M6 20.25h12A2.25 2.25 0 0 0 20.25 18V6A2.25 2.25 0 0 0 18 3.75H6A2.25 2.25 0 0 0 3.75 6v12A2.25 2.25 0 0 0 6 20.25Z" />
							</svg>
						</div>
						<h3 class="card-title text-xl mb-2">Extensible Core</h3>
						<p class="opacity-75">Built on a powerful PF4J plugin architecture. Easily extend capabilities to integrate with external APIs or custom game logic.</p>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- Status / Connection Section -->
	<div id="status" class="py-20 bg-base-200">
		<div class="container mx-auto px-4 text-center">
			<div class="max-w-xl mx-auto card bg-base-100 shadow-lg border-t-4 border-info">
				<div class="card-body">
					<h2 class="card-title justify-center text-2xl mb-4">System Status</h2>
					<div class="stat place-items-center p-0">
						<div class="stat-title text-lg">Backend API Connection</div>
						<div class="stat-value text-xl mt-2 flex items-center gap-2">
							{#if backendMessage === 'Checking connection...'}
								<span class="loading loading-spinner text-info loading-md"></span>
							{:else if backendMessage.includes('Failed')}
								<div class="badge badge-error gap-2 p-4">
									<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="inline-block w-4 h-4 stroke-current"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
									Offline
								</div>
							{:else}
								<div class="badge badge-success gap-2 p-4 text-white">
									<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="inline-block w-4 h-4 stroke-current"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
									Online
								</div>
							{/if}
						</div>
						<div class="stat-desc mt-4 w-full p-4 bg-base-200 rounded-lg text-left break-words">
							<span class="font-mono text-sm opacity-80">{backendMessage}</span>
						</div>
						<div class="stat-desc mt-4 opacity-50">Target: {apiUrl}</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- Footer -->
	<footer class="footer footer-center p-10 bg-neutral text-neutral-content rounded">
		<aside>
			<div class="font-bold text-2xl mb-2">Ark Resolver</div>
			<p>Group Party Management System</p>
			<p>Providing reliable coordination for your gaming sessions since 2026</p>
		</aside> 
		<nav>
			<div class="grid grid-flow-col gap-4">
				<!-- GitHub Icon -->
				<a href="https://github.com/NULL0xFF/gemini-does-code" class="hover:text-primary transition-colors">
					<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" class="fill-current"><path d="M12,2A10,10 0 0,0 8.84,21.5C9.34,21.58 9.5,21.27 9.5,21V19.31C6.73,19.91 6.14,18 6.14,18C5.69,16.86 5,16.5 5,16.5C4.06,15.86 5.08,15.88 5.08,15.88C6.1,15.95 6.64,16.92 6.64,16.92C7.55,18.47 9.02,18 9.57,17.77C9.66,17.15 9.9,16.69 10.17,16.42C7.94,16.17 5.6,15.31 5.6,11.5C5.6,10.4 5.92,9.5 6.45,8.8C6.36,8.54 6.09,7.5 6.54,6.26C6.54,6.26 7.23,6.04 9.06,7.27C9.72,7.09 10.43,7 11.14,7C11.85,7 12.56,7.09 13.22,7.27C15.05,6.04 15.74,6.26 15.74,6.26C16.19,7.5 15.92,8.54 15.83,8.8C16.36,9.5 16.68,10.4 16.68,11.5C16.68,15.33 14.33,16.16 12.1,16.41C12.43,16.7 12.72,17.26 12.72,18.14V21C12.72,21.28 12.89,21.59 13.38,21.5C16.24,20.55 18.39,17.84 18.5,14.67L18.5,14.5C18.5,10.91 15.59,8 12,8Z"></path></svg>
				</a>
			</div>
		</nav>
	</footer>
</div>
