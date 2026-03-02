<script lang="ts">
	import { base } from '$app/paths';
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
				</ul>
			</div>
			<a class="btn btn-ghost text-xl font-bold" href="{base}/">Ark Resolver</a>
		</div>
		<div class="navbar-center hidden lg:flex">
			<ul class="menu menu-horizontal px-1 font-semibold">
				<li><a href="#features">Features</a></li>
			</ul>
		</div>
		<div class="navbar-end gap-2">
			<label class="swap swap-rotate btn btn-ghost btn-circle">
				<input type="checkbox" class="theme-controller" value="dark" />
				<!-- sun icon -->
				<svg class="swap-off fill-current w-6 h-6" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M5.64,17l-.71.71a1,1,0,0,0,0,1.41,1,1,0,0,0,1.41,0l.71-.71A1,1,0,0,0,5.64,17ZM5,12a1,1,0,0,0-1-1H3a1,1,0,0,0,0,2H4A1,1,0,0,0,5,12Zm7-7a1,1,0,0,0,1-1V3a1,1,0,0,0-2,0V4A1,1,0,0,0,12,5ZM5.64,7.05a1,1,0,0,0,.7.29,1,1,0,0,0,.71-.29,1,1,0,0,0,0-1.41l-.71-.71A1,1,0,0,0,4.93,6.34Zm12,.29a1,1,0,0,0,.7-.29l.71-.71a1,1,0,1,0-1.41-1.41L17,5.64a1,1,0,0,0,0,1.41A1,1,0,0,0,17.66,7.34ZM21,11H20a1,1,0,0,0,0,2h1a1,1,0,0,0,0-2Zm-9,8a1,1,0,0,0-1,1v1a1,1,0,0,0,2,0V20A1,1,0,0,0,12,19ZM18.36,17A1,1,0,0,0,17,18.36l.71.71a1,1,0,0,0,1.41,0,1,1,0,0,0,0-1.41ZM12,6.5A5.5,5.5,0,1,0,17.5,12,5.51,5.51,0,0,0,12,6.5Zm0,9A3.5,3.5,0,1,1,15.5,12,3.5,3.5,0,0,1,12,15.5Z"/></svg>
				<!-- moon icon -->
				<svg class="swap-on fill-current w-6 h-6" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M21.64,13a1,1,0,0,0-1.05-.14,8.05,8.05,0,0,1-3.37.73A8.15,8.15,0,0,1,9.08,5.49a8.59,8.59,0,0,1,.25-2A1,1,0,0,0,8,2.36,10.14,10.14,0,1,0,22,14.05,1,1,0,0,0,21.64,13Zm-9.5,6.69A8.14,8.14,0,0,1,7.08,5.22v.27A10.15,10.15,0,0,0,17.22,15.63a9.79,9.79,0,0,0,2.1-.22A8.11,8.11,0,0,1,12.14,19.73Z"/></svg>
			</label>
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

	<!-- Footer -->
	<footer class="footer footer-center p-10 bg-neutral text-neutral-content rounded">
		<aside>
			<div class="font-bold text-2xl mb-2">Ark Resolver</div>
			<p>Group Party Management System</p>
			<p>Providing reliable coordination for your gaming sessions since 2026</p>
		</aside> 
	</footer>
</div>
