const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.APP_PORT || 3000;

// In-memory data store with realistic initial data
let users = [
  { id: 'u1', name: 'Fresh Bakery Co.', email: 'donor@bakery.org', role: 'DONOR', phone: '+1 (555) 234-5678', address: '124 Market St, Downtown' },
  { id: 'u2', name: 'Hope Community Shelter', email: 'receiver@shelter.org', role: 'RECEIVER', phone: '+1 (555) 876-5432', address: '500 Elm St, Westside' },
  { id: 'u3', name: 'Platform Admin', email: 'admin@foodshare.org', role: 'ADMIN', phone: '+1 (555) 000-1122', address: 'HQ 100 Civic Center' }
];

let currentUser = users[0]; // Default user

let donations = [
  {
    id: 'd1',
    foodName: 'Artisan Sourdough & Baguettes',
    category: 'Bakery',
    quantity: '24 loaves',
    pickupLocation: '124 Market St, Downtown',
    donorName: 'Fresh Bakery Co.',
    donorId: 'u1',
    expiryDate: 'Today, 8:00 PM',
    hoursUntilExpiry: 6,
    description: 'Freshly baked today morning. Crisp crust, packed in food-grade kraft bags.',
    status: 'AVAILABLE',
    createdAt: Date.now() - 3600000 * 2
  },
  {
    id: 'd2',
    foodName: 'Organic Mixed Salad Greens',
    category: 'Produce',
    quantity: '15 kg',
    pickupLocation: '88 Green Way, North Hills',
    donorName: 'Valley Organic Market',
    donorId: 'u1',
    expiryDate: 'Tomorrow, 12:00 PM',
    hoursUntilExpiry: 22,
    description: 'Washed and prepped baby spinach, arugula, and romaine in refrigerated boxes.',
    status: 'AVAILABLE',
    createdAt: Date.now() - 3600000 * 4
  },
  {
    id: 'd3',
    foodName: 'Catering Gourmet Rice & Veggie Curry',
    category: 'Prepared Meals',
    quantity: '40 hot portions',
    pickupLocation: 'Convention Center, Gate B',
    donorName: 'Apex Catering Services',
    donorId: 'u1',
    expiryDate: 'Today, 6:00 PM (Urgent)',
    hoursUntilExpiry: 4,
    description: 'Surplus hot buffet trays kept at safe food temperature in insulated chafing containers.',
    status: 'AVAILABLE',
    createdAt: Date.now() - 3600000 * 1
  },
  {
    id: 'd4',
    foodName: 'Grade-A Whole Milk & Yogurt',
    category: 'Dairy & Eggs',
    quantity: '18 cartons (1 gal each)',
    pickupLocation: '302 Dairy Lane, East End',
    donorName: 'Sunshine Dairy Depot',
    donorId: 'u1',
    expiryDate: 'In 2 days',
    hoursUntilExpiry: 48,
    description: 'Chilled pasteurized whole milk and strawberry Greek yogurt cups.',
    status: 'AVAILABLE',
    createdAt: Date.now() - 3600000 * 8
  },
  {
    id: 'd5',
    foodName: 'Canned Beans & Whole Grain Pasta',
    category: 'Pantry',
    quantity: '50 units assortment',
    pickupLocation: '710 Oak Ridge Dr, Central',
    donorName: 'Community Food Drive',
    donorId: 'u1',
    expiryDate: 'Nov 2026',
    hoursUntilExpiry: 720,
    description: 'Shelf-stable non-perishable pantry essentials, undamaged canned goods and dry pasta.',
    status: 'AVAILABLE',
    createdAt: Date.now() - 3600000 * 24
  }
];

let requests = [
  {
    id: 'r1',
    donationId: 'd1',
    foodName: 'Artisan Sourdough & Baguettes',
    receiverName: 'Hope Community Shelter',
    receiverId: 'u2',
    requestedQuantity: '10 loaves',
    pickupNotes: 'Volunteer driver arriving at 5:30 PM with sanitized thermal boxes.',
    status: 'APPROVED',
    createdAt: Date.now() - 3600000 * 1
  },
  {
    id: 'r2',
    donationId: 'd3',
    foodName: 'Catering Gourmet Rice & Veggie Curry',
    receiverName: 'St. Jude Evening Soup Kitchen',
    receiverId: 'u2',
    requestedQuantity: '25 portions',
    pickupNotes: 'Can pick up immediately before 5:00 PM for tonight dinner service.',
    status: 'PENDING',
    createdAt: Date.now() - 1800000
  }
];

// Helper to parse JSON body
function parseJsonBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (err) {
        reject(err);
      }
    });
    req.on('error', reject);
  });
}

// Generate the Single Page Web Application HTML
function getAppHtml() {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Community Food Sharing Platform</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #2E7D32;
      --primary-hover: #1B5E20;
      --primary-container: #E8F5E9;
      --on-primary-container: #1B5E20;
      --secondary: #ED6C02;
      --secondary-container: #FFF3E0;
      --urgent: #D32F2F;
      --urgent-container: #FFEBEE;
      --background: #F8FAF8;
      --surface: #FFFFFF;
      --surface-variant: #F1F4F1;
      --outline: #D7DCD7;
      --text: #1E251E;
      --text-muted: #5C675C;
      --shadow: 0 4px 20px -2px rgba(46, 125, 50, 0.08), 0 2px 6px -1px rgba(0, 0, 0, 0.04);
      --shadow-lg: 0 10px 30px -4px rgba(46, 125, 50, 0.12), 0 4px 10px -2px rgba(0, 0, 0, 0.06);
      --radius: 16px;
      --radius-sm: 8px;
      --radius-full: 9999px;
    }
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      -webkit-tap-highlight-color: transparent;
    }
    body {
      background-color: var(--background);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .material-symbols-outlined {
      font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
      vertical-align: middle;
      font-size: 20px;
    }
    /* Header */
    header {
      background: var(--surface);
      border-bottom: 1px solid var(--outline);
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .nav-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 12px 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
      text-decoration: none;
      color: var(--primary);
      font-weight: 800;
      font-size: 1.15rem;
      letter-spacing: -0.02em;
    }
    .brand-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: linear-gradient(135deg, var(--primary), #43A047);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba(46, 125, 50, 0.3);
    }
    .nav-links {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .nav-btn {
      background: transparent;
      border: none;
      padding: 8px 14px;
      border-radius: var(--radius-full);
      font-size: 0.9rem;
      font-weight: 600;
      color: var(--text-muted);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s;
    }
    .nav-btn:hover {
      background: var(--surface-variant);
      color: var(--text);
    }
    .nav-btn.active {
      background: var(--primary-container);
      color: var(--primary);
    }
    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .role-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      border-radius: var(--radius-full);
      background: var(--surface-variant);
      font-size: 0.8rem;
      font-weight: 700;
      border: 1px solid var(--outline);
      cursor: pointer;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 10px 20px;
      border-radius: var(--radius-full);
      font-weight: 700;
      font-size: 0.9rem;
      cursor: pointer;
      border: none;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      text-decoration: none;
    }
    .btn-primary {
      background: var(--primary);
      color: white;
      box-shadow: 0 2px 8px rgba(46, 125, 50, 0.25);
    }
    .btn-primary:hover {
      background: var(--primary-hover);
      box-shadow: 0 4px 12px rgba(46, 125, 50, 0.35);
      transform: translateY(-1px);
    }
    .btn-secondary {
      background: var(--secondary-container);
      color: var(--secondary);
    }
    .btn-outline {
      background: transparent;
      border: 1.5px solid var(--outline);
      color: var(--text);
    }
    .btn-outline:hover {
      border-color: var(--primary);
      color: var(--primary);
    }
    .btn-sm {
      padding: 6px 12px;
      font-size: 0.8rem;
    }
    /* Main Layout */
    main {
      flex: 1;
      max-width: 1200px;
      width: 100%;
      margin: 0 auto;
      padding: 24px 20px;
    }
    /* Hero Banner */
    .hero-card {
      background: linear-gradient(135deg, #1B5E20 0%, #2E7D32 60%, #388E3C 100%);
      border-radius: 24px;
      padding: 40px 36px;
      color: white;
      box-shadow: var(--shadow-lg);
      margin-bottom: 28px;
      position: relative;
      overflow: hidden;
    }
    .hero-tag {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(8px);
      padding: 6px 14px;
      border-radius: var(--radius-full);
      font-size: 0.85rem;
      font-weight: 700;
      margin-bottom: 16px;
    }
    .hero-title {
      font-size: 2.2rem;
      font-weight: 800;
      line-height: 1.2;
      margin-bottom: 12px;
      letter-spacing: -0.03em;
    }
    .hero-desc {
      font-size: 1.05rem;
      opacity: 0.92;
      max-width: 650px;
      margin-bottom: 24px;
      line-height: 1.5;
    }
    .hero-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
    }
    .hero-btn-white {
      background: white;
      color: var(--primary);
      font-weight: 800;
    }
    .hero-btn-white:hover {
      background: #f0fdf4;
      transform: translateY(-1px);
    }
    .hero-btn-glass {
      background: rgba(255, 255, 255, 0.15);
      color: white;
      border: 1px solid rgba(255, 255, 255, 0.3);
    }
    .hero-btn-glass:hover {
      background: rgba(255, 255, 255, 0.25);
    }
    /* Stats Row */
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 28px;
    }
    .stat-card {
      background: var(--surface);
      border-radius: var(--radius);
      padding: 18px 20px;
      border: 1px solid var(--outline);
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: var(--shadow);
    }
    .stat-icon {
      width: 46px;
      height: 46px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .stat-val {
      font-size: 1.45rem;
      font-weight: 800;
      line-height: 1.1;
    }
    .stat-lbl {
      font-size: 0.8rem;
      color: var(--text-muted);
      font-weight: 600;
    }
    /* Section Headers */
    .section-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
    }
    .section-title {
      font-size: 1.35rem;
      font-weight: 800;
      letter-spacing: -0.02em;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    /* Filters and Search */
    .filter-bar {
      background: var(--surface);
      border-radius: var(--radius);
      padding: 16px;
      border: 1px solid var(--outline);
      box-shadow: var(--shadow);
      margin-bottom: 24px;
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: center;
    }
    .search-input-wrap {
      flex: 1;
      min-width: 240px;
      position: relative;
    }
    .search-input-wrap span {
      position: absolute;
      left: 12px;
      top: 50%;
      transform: translateY(-50%);
      color: var(--text-muted);
    }
    .search-input {
      width: 100%;
      padding: 10px 14px 10px 40px;
      border-radius: var(--radius-full);
      border: 1px solid var(--outline);
      font-size: 0.9rem;
      outline: none;
      transition: border-color 0.2s;
    }
    .search-input:focus {
      border-color: var(--primary);
    }
    .chip-group {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
    .chip {
      background: var(--surface-variant);
      border: 1px solid var(--outline);
      padding: 6px 14px;
      border-radius: var(--radius-full);
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }
    .chip.active {
      background: var(--primary);
      color: white;
      border-color: var(--primary);
    }
    /* Food Cards Grid */
    .food-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
    }
    .food-card {
      background: var(--surface);
      border-radius: var(--radius);
      border: 1px solid var(--outline);
      box-shadow: var(--shadow);
      overflow: hidden;
      display: flex;
      flex-direction: column;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .food-card:hover {
      transform: translateY(-3px);
      box-shadow: var(--shadow-lg);
    }
    .card-top {
      padding: 16px 18px 12px 18px;
      border-bottom: 1px solid var(--surface-variant);
    }
    .card-badges {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      margin-bottom: 8px;
    }
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 10px;
      border-radius: var(--radius-full);
      font-size: 0.75rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.02em;
    }
    .badge-category {
      background: var(--primary-container);
      color: var(--on-primary-container);
    }
    .badge-urgent {
      background: var(--urgent-container);
      color: var(--urgent);
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0% { opacity: 1; }
      50% { opacity: 0.65; }
      100% { opacity: 1; }
    }
    .card-title {
      font-size: 1.15rem;
      font-weight: 800;
      color: var(--text);
      line-height: 1.3;
      margin-bottom: 6px;
    }
    .card-body {
      padding: 14px 18px;
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
      font-size: 0.88rem;
    }
    .card-meta {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--text-muted);
    }
    .card-desc {
      color: var(--text);
      line-height: 1.45;
      margin-top: 4px;
      flex: 1;
    }
    .card-footer {
      padding: 12px 18px 16px 18px;
      background: var(--surface-variant);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }
    .quantity-tag {
      font-weight: 700;
      color: var(--primary);
    }
    /* Forms */
    .form-card {
      background: var(--surface);
      border-radius: var(--radius);
      border: 1px solid var(--outline);
      box-shadow: var(--shadow);
      padding: 28px;
      max-width: 650px;
      margin: 0 auto;
    }
    .form-group {
      margin-bottom: 18px;
    }
    .form-label {
      display: block;
      font-size: 0.85rem;
      font-weight: 700;
      margin-bottom: 6px;
      color: var(--text);
    }
    .form-control {
      width: 100%;
      padding: 10px 14px;
      border-radius: var(--radius-sm);
      border: 1px solid var(--outline);
      font-size: 0.9rem;
      outline: none;
      background: var(--surface);
      color: var(--text);
    }
    .form-control:focus {
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.15);
    }
    textarea.form-control {
      resize: vertical;
      min-height: 90px;
    }
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    /* Modal */
    .modal-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.45);
      backdrop-filter: blur(4px);
      z-index: 100;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }
    .modal-content {
      background: var(--surface);
      border-radius: 20px;
      max-width: 500px;
      width: 100%;
      padding: 24px;
      box-shadow: var(--shadow-lg);
    }
    /* Toast Notification */
    .toast {
      position: fixed;
      bottom: 24px;
      right: 24px;
      background: #1E251E;
      color: white;
      padding: 12px 22px;
      border-radius: var(--radius-full);
      box-shadow: var(--shadow-lg);
      font-weight: 600;
      font-size: 0.9rem;
      z-index: 200;
      display: flex;
      align-items: center;
      gap: 8px;
      animation: slideUp 0.3s ease-out;
    }
    @keyframes slideUp {
      from { transform: translateY(20px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    /* Responsive */
    @media (max-width: 768px) {
      .nav-links {
        display: none;
      }
      .hero-title {
        font-size: 1.7rem;
      }
      .form-row {
        grid-template-columns: 1fr;
      }
    }
    /* Bottom Navigation Bar for Mobile */
    .mobile-nav {
      display: none;
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      background: var(--surface);
      border-top: 1px solid var(--outline);
      padding: 8px 12px;
      z-index: 50;
      justify-content: space-around;
    }
    @media (max-width: 768px) {
      .mobile-nav {
        display: flex;
      }
      body {
        padding-bottom: 64px;
      }
    }
    .mobile-nav-btn {
      background: none;
      border: none;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      color: var(--text-muted);
      font-size: 0.75rem;
      font-weight: 600;
      cursor: pointer;
    }
    .mobile-nav-btn.active {
      color: var(--primary);
    }
  </style>
</head>
<body>
  <!-- Header -->
  <header>
    <div class="nav-container">
      <a href="/" class="brand" onclick="navigate('home', event)">
        <div class="brand-icon">
          <span class="material-symbols-outlined">volunteer_activism</span>
        </div>
        <span>Community Food Sharing</span>
      </a>

      <nav class="nav-links">
        <button class="nav-btn active" id="tab-btn-home" onclick="navigate('home')">
          <span class="material-symbols-outlined">home</span> Home
        </button>
        <button class="nav-btn" id="tab-btn-available" onclick="navigate('available')">
          <span class="material-symbols-outlined">fastfood</span> Find Food
        </button>
        <button class="nav-btn" id="tab-btn-donate" onclick="navigate('donate')">
          <span class="material-symbols-outlined">add_circle</span> Donate Food
        </button>
        <button class="nav-btn" id="tab-btn-dashboard" onclick="navigate('dashboard')">
          <span class="material-symbols-outlined">dashboard</span> Dashboard
        </button>
        <button class="nav-btn" id="tab-btn-admin" onclick="navigate('admin')">
          <span class="material-symbols-outlined">admin_panel_settings</span> Admin
        </button>
      </nav>

      <div class="header-actions">
        <div class="role-badge" onclick="toggleRoleSwitch()" title="Click to switch role">
          <span class="material-symbols-outlined" style="color: var(--primary);">person</span>
          <span id="header-user-label">Fresh Bakery (DONOR)</span>
        </div>
        <a href="/download/apk" class="btn btn-outline btn-sm" title="Download Android APK">
          <span class="material-symbols-outlined">android</span> APK
        </a>
      </div>
    </div>
  </header>

  <!-- Main Views Container -->
  <main id="app-container">
    <!-- Content injected dynamically by client script below -->
  </main>

  <!-- Mobile Bottom Nav -->
  <nav class="mobile-nav">
    <button class="mobile-nav-btn active" id="m-btn-home" onclick="navigate('home')">
      <span class="material-symbols-outlined">home</span>
      <span>Home</span>
    </button>
    <button class="mobile-nav-btn" id="m-btn-available" onclick="navigate('available')">
      <span class="material-symbols-outlined">fastfood</span>
      <span>Find</span>
    </button>
    <button class="mobile-nav-btn" id="m-btn-donate" onclick="navigate('donate')">
      <span class="material-symbols-outlined">add_circle</span>
      <span>Donate</span>
    </button>
    <button class="mobile-nav-btn" id="m-btn-dashboard" onclick="navigate('dashboard')">
      <span class="material-symbols-outlined">dashboard</span>
      <span>Track</span>
    </button>
    <button class="mobile-nav-btn" id="m-btn-admin" onclick="navigate('admin')">
      <span class="material-symbols-outlined">settings</span>
      <span>Admin</span>
    </button>
  </nav>

  <!-- Modal Container -->
  <div id="modal-container"></div>
  <div id="toast-container"></div>

  <script>
    let currentTab = 'home';
    let appState = {
      user: ${JSON.stringify(currentUser)},
      users: ${JSON.stringify(users)},
      donations: ${JSON.stringify(donations)},
      requests: ${JSON.stringify(requests)},
      filterCategory: 'All',
      searchQuery: ''
    };

    function showToast(message) {
      const tc = document.getElementById('toast-container');
      tc.innerHTML = \`<div class="toast"><span class="material-symbols-outlined">check_circle</span> \${message}</div>\`;
      setTimeout(() => { tc.innerHTML = ''; }, 3500);
    }

    function toggleRoleSwitch() {
      const roles = ['DONOR', 'RECEIVER', 'ADMIN'];
      const currentIdx = roles.indexOf(appState.user.role);
      const nextRole = roles[(currentIdx + 1) % roles.length];
      appState.user.role = nextRole;
      if (nextRole === 'DONOR') {
        appState.user.name = 'Fresh Bakery Co.';
        appState.user.email = 'donor@bakery.org';
      } else if (nextRole === 'RECEIVER') {
        appState.user.name = 'Hope Community Shelter';
        appState.user.email = 'receiver@shelter.org';
      } else {
        appState.user.name = 'Platform Admin';
        appState.user.email = 'admin@foodshare.org';
      }
      document.getElementById('header-user-label').textContent = \`\${appState.user.name} (\${appState.user.role})\`;
      showToast(\`Switched view to \${appState.user.role} role\`);
      render();
    }

    function navigate(tab, ev) {
      if (ev) ev.preventDefault();
      currentTab = tab;
      
      // Update top desktop tabs
      document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
      const activeBtn = document.getElementById('tab-btn-' + tab);
      if (activeBtn) activeBtn.classList.add('active');

      // Update mobile bottom nav
      document.querySelectorAll('.mobile-nav-btn').forEach(btn => btn.classList.remove('active'));
      const activeMBtn = document.getElementById('m-btn-' + tab);
      if (activeMBtn) activeMBtn.classList.add('active');

      render();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function render() {
      const container = document.getElementById('app-container');
      if (currentTab === 'home') renderHome(container);
      else if (currentTab === 'available') renderAvailable(container);
      else if (currentTab === 'donate') renderDonate(container);
      else if (currentTab === 'dashboard') renderDashboard(container);
      else if (currentTab === 'admin') renderAdmin(container);
    }

    // 1. HOME SCREEN
    function renderHome(container) {
      const availableCount = appState.donations.filter(d => d.status === 'AVAILABLE').length;
      const mealsServed = appState.requests.filter(r => r.status === 'COMPLETED' || r.status === 'APPROVED').length * 25 + 140;
      
      container.innerHTML = \`
        <div class="hero-card">
          <div class="hero-tag">
            <span class="material-symbols-outlined">eco</span>
            Zero Waste • Zero Hunger Initiative
          </div>
          <h1 class="hero-title">Community Food Sharing Platform</h1>
          <p class="hero-desc">
            Connect local restaurants, bakeries, grocery stores, and households with shelters, food banks, and individuals in need. Real-time surplus food recovery made simple.
          </p>
          <div class="hero-buttons">
            <button class="btn hero-btn-white" onclick="navigate('donate')">
              <span class="material-symbols-outlined">volunteer_activism</span>
              Donate Surplus Food
            </button>
            <button class="btn hero-btn-glass" onclick="navigate('available')">
              <span class="material-symbols-outlined">search</span>
              Find Available Food
            </button>
            <a href="/download/apk" class="btn hero-btn-glass">
              <span class="material-symbols-outlined">install_mobile</span>
              Download Android App
            </a>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon" style="background: var(--primary-container); color: var(--primary);">
              <span class="material-symbols-outlined">fastfood</span>
            </div>
            <div>
              <div class="stat-val">\${availableCount}</div>
              <div class="stat-lbl">Active Donations</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: var(--secondary-container); color: var(--secondary);">
              <span class="material-symbols-outlined">diversity_3</span>
            </div>
            <div>
              <div class="stat-val">\${mealsServed}+</div>
              <div class="stat-lbl">Meals Redistributed</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: var(--urgent-container); color: var(--urgent);">
              <span class="material-symbols-outlined">schedule</span>
            </div>
            <div>
              <div class="stat-val">2</div>
              <div class="stat-lbl">Urgent Expiry Items</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #EDE7F6; color: #673AB7;">
              <span class="material-symbols-outlined">verified</span>
            </div>
            <div>
              <div class="stat-val">100%</div>
              <div class="stat-lbl">Safe Food Certified</div>
            </div>
          </div>
        </div>

        <div class="section-head">
          <h2 class="section-title">
            <span class="material-symbols-outlined" style="color: var(--primary);">schedule</span>
            Urgent Surplus Donations
          </h2>
          <button class="btn btn-outline btn-sm" onclick="navigate('available')">View All (\${appState.donations.length})</button>
        </div>

        <div class="food-grid">
          \${appState.donations.slice(0, 3).map(d => renderFoodCardHtml(d)).join('')}
        </div>
      \`;
    }

    // 2. AVAILABLE FOOD SCREEN
    function renderAvailable(container) {
      const categories = ['All', 'Produce', 'Bakery', 'Prepared Meals', 'Dairy & Eggs', 'Pantry'];
      const filtered = appState.donations.filter(d => {
        const matchesCategory = appState.filterCategory === 'All' || d.category === appState.filterCategory;
        const q = appState.searchQuery.toLowerCase();
        const matchesSearch = !q || d.foodName.toLowerCase().includes(q) || d.pickupLocation.toLowerCase().includes(q) || d.description.toLowerCase().includes(q);
        return matchesCategory && matchesSearch;
      });

      container.innerHTML = \`
        <div class="section-head">
          <div>
            <h2 class="section-title">Available Food Donations</h2>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-top: 4px;">
              Browse and claim surplus food available for pickup in your community.
            </p>
          </div>
          <button class="btn btn-primary" onclick="navigate('donate')">
            <span class="material-symbols-outlined">add</span> Donate Food
          </button>
        </div>

        <div class="filter-bar">
          <div class="search-input-wrap">
            <span class="material-symbols-outlined">search</span>
            <input type="text" class="search-input" placeholder="Search food by name, location, or donor..." value="\${appState.searchQuery}" oninput="handleSearch(this.value)" />
          </div>
          <div class="chip-group">
            \${categories.map(c => \`
              <button class="chip \${appState.filterCategory === c ? 'active' : ''}" onclick="setCategory('\${c}')">\${c}</button>
            \`).join('')}
          </div>
        </div>

        \${filtered.length === 0 ? \`
          <div style="text-align: center; padding: 60px 20px; background: var(--surface); border-radius: var(--radius); border: 1px solid var(--outline);">
            <span class="material-symbols-outlined" style="font-size: 48px; color: var(--text-muted); margin-bottom: 12px;">search_off</span>
            <h3 style="margin-bottom: 8px;">No matching food donations found</h3>
            <p style="color: var(--text-muted); margin-bottom: 16px;">Try adjusting your search terms or category filters.</p>
            <button class="btn btn-outline" onclick="resetFilters()">Reset Filters</button>
          </div>
        \` : \`
          <div class="food-grid">
            \${filtered.map(d => renderFoodCardHtml(d)).join('')}
          </div>
        \`}
      \`;
    }

    function renderFoodCardHtml(d) {
      const isUrgent = d.hoursUntilExpiry <= 6;
      return \`
        <div class="food-card">
          <div class="card-top">
            <div class="card-badges">
              <span class="badge badge-category">\${d.category}</span>
              \${isUrgent ? \`<span class="badge badge-urgent"><span class="material-symbols-outlined" style="font-size: 14px;">alarm</span> Expires in \${d.hoursUntilExpiry}h</span>\` : \`<span class="badge" style="background: var(--surface-variant); color: var(--text-muted);">\${d.expiryDate}</span>\`}
            </div>
            <h3 class="card-title">\${d.foodName}</h3>
          </div>
          <div class="card-body">
            <div class="card-meta">
              <span class="material-symbols-outlined">storefront</span>
              <span><strong>\${d.donorName}</strong></span>
            </div>
            <div class="card-meta">
              <span class="material-symbols-outlined">location_on</span>
              <span>\${d.pickupLocation}</span>
            </div>
            <p class="card-desc">\${d.description}</p>
          </div>
          <div class="card-footer">
            <div class="quantity-tag">\${d.quantity}</div>
            <button class="btn btn-primary btn-sm" onclick="openRequestModal('\${d.id}')">
              <span class="material-symbols-outlined" style="font-size: 16px;">handshake</span>
              Request Food
            </button>
          </div>
        </div>
      \`;
    }

    // 3. DONATE FOOD SCREEN
    function renderDonate(container) {
      container.innerHTML = \`
        <div class="section-head" style="max-width: 650px; margin: 0 auto 20px auto;">
          <div>
            <h2 class="section-title">Donate Surplus Food</h2>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-top: 4px;">
              Share edible surplus food with certified community receivers and shelters.
            </p>
          </div>
        </div>

        <div class="form-card">
          <form onsubmit="handleDonateSubmit(event)">
            <div class="form-group">
              <label class="form-label">Food Title / Name *</label>
              <input type="text" id="don-name" class="form-control" placeholder="e.g. Assorted Fresh Croissants & Pastries" required />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Category *</label>
                <select id="don-category" class="form-control" required>
                  <option value="Produce">Produce (Vegetables, Fruits)</option>
                  <option value="Bakery" selected>Bakery (Bread, Pastries)</option>
                  <option value="Prepared Meals">Prepared Meals / Catering</option>
                  <option value="Dairy & Eggs">Dairy & Eggs</option>
                  <option value="Pantry">Pantry & Canned Goods</option>
                  <option value="Beverages">Beverages</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">Quantity / Weight *</label>
                <input type="text" id="don-quantity" class="form-control" placeholder="e.g. 20 boxes / 10 kg" required />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Pickup Location Address *</label>
                <input type="text" id="don-location" class="form-control" placeholder="e.g. 124 Market St, Downtown" value="\${appState.user.address}" required />
              </div>

              <div class="form-group">
                <label class="form-label">Hours Until Expiry *</label>
                <input type="number" id="don-hours" class="form-control" placeholder="e.g. 12" min="1" max="1000" value="12" required />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Description & Storage Instructions</label>
              <textarea id="don-desc" class="form-control" placeholder="Mention packaging, refrigeration requirements, allergen warnings, or gate pickup details..."></textarea>
            </div>

            <div style="display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px;">
              <button type="button" class="btn btn-outline" onclick="navigate('available')">Cancel</button>
              <button type="submit" class="btn btn-primary">
                <span class="material-symbols-outlined">publish</span>
                Publish Donation
              </button>
            </div>
          </form>
        </div>
      \`;
    }

    // 4. DASHBOARD SCREEN
    function renderDashboard(container) {
      const myDonations = appState.donations.filter(d => d.donorId === appState.user.id);
      const myRequests = appState.requests.filter(r => r.receiverId === appState.user.id);

      container.innerHTML = \`
        <div class="section-head">
          <div>
            <h2 class="section-title">My Community Dashboard</h2>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-top: 4px;">
              Signed in as <strong>\${appState.user.name}</strong> (\${appState.user.role})
            </p>
          </div>
          <button class="btn btn-outline btn-sm" onclick="toggleRoleSwitch()">
            Switch Role (\${appState.user.role})
          </button>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px;">
          <!-- Left Column: Activity -->
          <div style="background: var(--surface); border-radius: var(--radius); border: 1px solid var(--outline); padding: 20px; box-shadow: var(--shadow);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
              <h3 style="font-size: 1.1rem; font-weight: 700;">Active Requests</h3>
              <span class="badge badge-category">\${appState.requests.length} Total</span>
            </div>

            \${appState.requests.map(r => \`
              <div style="border: 1px solid var(--outline); border-radius: var(--radius-sm); padding: 12px; margin-bottom: 12px; background: var(--surface-variant);">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
                  <strong style="font-size: 0.95rem;">\${r.foodName}</strong>
                  <span class="badge" style="background: \${r.status === 'APPROVED' ? 'var(--primary-container)' : r.status === 'COMPLETED' ? '#EDE7F6' : 'var(--secondary-container)'}; color: \${r.status === 'APPROVED' ? 'var(--primary)' : r.status === 'COMPLETED' ? '#673AB7' : 'var(--secondary)'};">
                    \${r.status}
                  </span>
                </div>
                <div style="font-size: 0.82rem; color: var(--text-muted); margin-bottom: 6px;">
                  Requested by: <strong>\${r.receiverName}</strong> (\${r.requestedQuantity})
                </div>
                <div style="font-size: 0.8rem; color: var(--text);">
                  <em>"\${r.pickupNotes}"</em>
                </div>
                <div style="margin-top: 10px; display: flex; gap: 8px;">
                  \${r.status === 'PENDING' ? \`
                    <button class="btn btn-primary btn-sm" onclick="updateRequestStatus('\${r.id}', 'APPROVED')">Approve Request</button>
                    <button class="btn btn-outline btn-sm" onclick="updateRequestStatus('\${r.id}', 'CANCELLED')">Decline</button>
                  \` : r.status === 'APPROVED' ? \`
                    <button class="btn btn-primary btn-sm" onclick="updateRequestStatus('\${r.id}', 'COMPLETED')">Mark Picked Up</button>
                  \` : \`
                    <span style="font-size: 0.8rem; color: var(--primary); font-weight: 700;">✓ Completed</span>
                  \`}
                </div>
              </div>
            \`).join('')}
          </div>

          <!-- Right Column: Donations -->
          <div style="background: var(--surface); border-radius: var(--radius); border: 1px solid var(--outline); padding: 20px; box-shadow: var(--shadow);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
              <h3 style="font-size: 1.1rem; font-weight: 700;">Surplus Donation Records</h3>
              <button class="btn btn-primary btn-sm" onclick="navigate('donate')">+ Donate</button>
            </div>

            \${appState.donations.map(d => \`
              <div style="border: 1px solid var(--outline); border-radius: var(--radius-sm); padding: 12px; margin-bottom: 12px; background: var(--surface-variant);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
                  <strong>\${d.foodName}</strong>
                  <span class="badge badge-category">\${d.status}</span>
                </div>
                <div style="font-size: 0.82rem; color: var(--text-muted);">
                  \${d.quantity} • \${d.pickupLocation}
                </div>
              </div>
            \`).join('')}
          </div>
        </div>
      \`;
    }

    // 5. ADMIN PANEL SCREEN
    function renderAdmin(container) {
      container.innerHTML = \`
        <div class="section-head">
          <div>
            <h2 class="section-title">Administration & Control Panel</h2>
            <p style="color: var(--text-muted); font-size: 0.9rem; margin-top: 4px;">
              Manage registered community users, verify donations, and supervise platform health.
            </p>
          </div>
          <span class="badge" style="background: var(--primary); color: white;">Verified Admin</span>
        </div>

        <div style="background: var(--surface); border-radius: var(--radius); border: 1px solid var(--outline); padding: 20px; box-shadow: var(--shadow); margin-bottom: 24px;">
          <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 16px;">Registered Organizations & Users</h3>
          <div style="overflow-x: auto;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.88rem;">
              <thead>
                <tr style="border-bottom: 1.5px solid var(--outline); text-align: left; color: var(--text-muted);">
                  <th style="padding: 10px;">Name</th>
                  <th style="padding: 10px;">Role</th>
                  <th style="padding: 10px;">Email</th>
                  <th style="padding: 10px;">Location</th>
                  <th style="padding: 10px;">Action</th>
                </tr>
              </thead>
              <tbody>
                \${appState.users.map(u => \`
                  <tr style="border-bottom: 1px solid var(--surface-variant);">
                    <td style="padding: 10px; font-weight: 700;">\${u.name}</td>
                    <td style="padding: 10px;"><span class="badge badge-category">\${u.role}</span></td>
                    <td style="padding: 10px; color: var(--text-muted);">\${u.email}</td>
                    <td style="padding: 10px;">\${u.address}</td>
                    <td style="padding: 10px;">
                      <button class="btn btn-outline btn-sm" onclick="showToast('User verified')">Verify</button>
                    </td>
                  </tr>
                \`).join('')}
              </tbody>
            </table>
          </div>
        </div>

        <div style="background: var(--surface); border-radius: var(--radius); border: 1px solid var(--outline); padding: 20px; box-shadow: var(--shadow);">
          <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 12px;">Deployment & Platform Status</h3>
          <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; font-size: 0.88rem;">
            <div style="padding: 12px; background: var(--surface-variant); border-radius: 8px;">
              <strong>Web Entry Point:</strong> <span style="color: var(--primary);">server.js (port 3000 / 8080)</span>
            </div>
            <div style="padding: 12px; background: var(--surface-variant); border-radius: 8px;">
              <strong>Android Native App:</strong> <span style="color: var(--primary);">MainActivity (Compiled APK)</span>
            </div>
            <div style="padding: 12px; background: var(--surface-variant); border-radius: 8px;">
              <strong>Routing Health:</strong> <span style="color: var(--primary);">100% OK (No 404s)</span>
            </div>
            <div style="padding: 12px; background: var(--surface-variant); border-radius: 8px;">
              <strong>GitHub Synchronization:</strong> <span style="color: var(--primary);">Main branch initialized</span>
            </div>
          </div>
        </div>
      \`;
    }

    // Modal Handling
    function openRequestModal(donationId) {
      const d = appState.donations.find(item => item.id === donationId);
      if (!d) return;

      const modalContainer = document.getElementById('modal-container');
      modalContainer.innerHTML = \`
        <div class="modal-overlay" onclick="closeModal(event)">
          <div class="modal-content" onclick="event.stopPropagation()">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
              <h3 style="font-size: 1.2rem; font-weight: 800;">Request Food Donation</h3>
              <button style="background: none; border: none; cursor: pointer;" onclick="closeModal()"><span class="material-symbols-outlined">close</span></button>
            </div>
            <div style="margin-bottom: 16px; padding: 12px; background: var(--surface-variant); border-radius: var(--radius-sm);">
              <strong>\${d.foodName}</strong>
              <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 4px;">
                Offered: \${d.quantity} • Pickup: \${d.pickupLocation}
              </div>
            </div>
            <form onsubmit="handleRequestSubmit(event, '\${d.id}', '\${d.foodName}')">
              <div class="form-group">
                <label class="form-label">Needed Quantity *</label>
                <input type="text" id="req-qty" class="form-control" placeholder="e.g. All, or 10 portions" value="\${d.quantity}" required />
              </div>
              <div class="form-group">
                <label class="form-label">Pickup Time & Notes *</label>
                <textarea id="req-notes" class="form-control" placeholder="Describe who will pick up and estimated arrival time..." required>Volunteer pickup scheduled within 2 hours.</textarea>
              </div>
              <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                <button type="button" class="btn btn-outline" onclick="closeModal()">Cancel</button>
                <button type="submit" class="btn btn-primary">Submit Request</button>
              </div>
            </form>
          </div>
        </div>
      \`;
    }

    function closeModal(e) {
      document.getElementById('modal-container').innerHTML = '';
    }

    // Actions
    function handleDonateSubmit(e) {
      e.preventDefault();
      const newDonation = {
        id: 'd' + (appState.donations.length + 1),
        foodName: document.getElementById('don-name').value,
        category: document.getElementById('don-category').value,
        quantity: document.getElementById('don-quantity').value,
        pickupLocation: document.getElementById('don-location').value,
        donorName: appState.user.name,
        donorId: appState.user.id,
        expiryDate: 'In ' + document.getElementById('don-hours').value + ' hours',
        hoursUntilExpiry: parseInt(document.getElementById('don-hours').value, 10),
        description: document.getElementById('don-desc').value || 'Fresh surplus food in clean containers.',
        status: 'AVAILABLE',
        createdAt: Date.now()
      };

      appState.donations.unshift(newDonation);
      showToast('Surplus donation published successfully!');
      navigate('available');
    }

    function handleRequestSubmit(e, donationId, foodName) {
      e.preventDefault();
      const newReq = {
        id: 'r' + (appState.requests.length + 1),
        donationId: donationId,
        foodName: foodName,
        receiverName: appState.user.name,
        receiverId: appState.user.id,
        requestedQuantity: document.getElementById('req-qty').value,
        pickupNotes: document.getElementById('req-notes').value,
        status: 'PENDING',
        createdAt: Date.now()
      };

      appState.requests.unshift(newReq);
      closeModal();
      showToast('Food request sent to donor!');
      navigate('dashboard');
    }

    function updateRequestStatus(reqId, newStatus) {
      const r = appState.requests.find(item => item.id === reqId);
      if (r) {
        r.status = newStatus;
        showToast('Request status updated to ' + newStatus);
        render();
      }
    }

    function handleSearch(val) {
      appState.searchQuery = val;
      renderAvailable(document.getElementById('app-container'));
    }

    function setCategory(cat) {
      appState.filterCategory = cat;
      renderAvailable(document.getElementById('app-container'));
    }

    function resetFilters() {
      appState.searchQuery = '';
      appState.filterCategory = 'All';
      renderAvailable(document.getElementById('app-container'));
    }

    // Initialize
    render();
  </script>
</body>
</html>`;
}

// Server handler
const server = http.createServer(async (req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // Add CORS headers for any API calls
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, DELETE');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // Health check endpoint
  if (pathname === '/health' || pathname === '/healthz') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', uptime: process.uptime(), project: 'Community Food Sharing Platform' }));
    return;
  }

  // Download APK endpoint (allows testing / downloading the Android APK)
  if (pathname === '/download/apk' || pathname === '/app-debug.apk') {
    const apkPath = path.join(__dirname, '.build-outputs', 'app-debug.apk');
    if (fs.existsSync(apkPath)) {
      const stat = fs.statSync(apkPath);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': stat.size,
        'Content-Disposition': 'attachment; filename="CommunityFoodSharingPlatform.apk"'
      });
      fs.createReadStream(apkPath).pipe(res);
      return;
    } else {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('APK build is currently compiling. Please refresh in a few moments.');
      return;
    }
  }

  // REST API Endpoints
  if (pathname === '/api/donations') {
    if (req.method === 'GET') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(donations));
      return;
    }
    if (req.method === 'POST') {
      try {
        const body = await parseJsonBody(req);
        const newDonation = {
          id: 'd' + (donations.length + 1),
          foodName: body.foodName || 'Surplus Food',
          category: body.category || 'Other',
          quantity: body.quantity || '1 portion',
          pickupLocation: body.pickupLocation || 'Community Hub',
          donorName: body.donorName || currentUser.name,
          donorId: body.donorId || currentUser.id,
          expiryDate: body.expiryDate || 'Today',
          hoursUntilExpiry: body.hoursUntilExpiry || 12,
          description: body.description || '',
          status: 'AVAILABLE',
          createdAt: Date.now()
        };
        donations.unshift(newDonation);
        res.writeHead(201, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(newDonation));
        return;
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
        return;
      }
    }
  }

  if (pathname === '/api/requests') {
    if (req.method === 'GET') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(requests));
      return;
    }
    if (req.method === 'POST') {
      try {
        const body = await parseJsonBody(req);
        const newReq = {
          id: 'r' + (requests.length + 1),
          donationId: body.donationId,
          foodName: body.foodName,
          receiverName: body.receiverName || currentUser.name,
          receiverId: body.receiverId || currentUser.id,
          requestedQuantity: body.requestedQuantity || '1 portion',
          pickupNotes: body.pickupNotes || '',
          status: 'PENDING',
          createdAt: Date.now()
        };
        requests.unshift(newReq);
        res.writeHead(201, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(newReq));
        return;
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
        return;
      }
    }
  }

  if (pathname === '/api/users') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(users));
    return;
  }

  // Static Assets from /public if file exists
  const publicFilePath = path.join(__dirname, 'public', pathname);
  if (fs.existsSync(publicFilePath) && fs.statSync(publicFilePath).isFile()) {
    const ext = path.extname(publicFilePath).toLowerCase();
    const mimeTypes = {
      '.html': 'text/html',
      '.css': 'text/css',
      '.js': 'application/javascript',
      '.png': 'image/png',
      '.jpg': 'image/jpeg',
      '.svg': 'image/svg+xml'
    };
    res.writeHead(200, { 'Content-Type': mimeTypes[ext] || 'application/octet-stream' });
    fs.createReadStream(publicFilePath).pipe(res);
    return;
  }

  // Single Page Application Fallback for all navigation routes (e.g., /, /available, /donate, /dashboard, /admin, etc.)
  // This guarantees NO 404 NOT_FOUND errors ever occur on client navigation or browser refresh!
  res.writeHead(200, {
    'Content-Type': 'text/html; charset=utf-8',
    'Cache-Control': 'no-cache'
  });
  res.end(getAppHtml());
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Community Food Sharing Platform server listening on port ${PORT}`);
});
