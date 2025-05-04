// User Profile Management Script

// DOM Elements
let profileForm, passwordForm, profileRetryBtn;
let profileInfoTab, changePasswordTab;
let profileLoading, profileError, profileErrorMessage;

// Initialize profile page
document.addEventListener('DOMContentLoaded', () => {
    // Initialize DOM elements
    initDomElements();
    
    // Set up event listeners
    setupProfileEventListeners();
    
    // Check authentication
    checkProfileAuth();
    
    // Load profile data
    loadUserProfile();
    
    // Initialize user avatar with current username
    updateUserAvatar();
});

// Initialize DOM elements
function initDomElements() {
    // Tab elements
    profileInfoTab = document.querySelector('.tab-btn[data-tab="profile-info"]');
    changePasswordTab = document.querySelector('.tab-btn[data-tab="change-password"]');
    
    // Form elements
    profileForm = document.getElementById('update-profile-form');
    passwordForm = document.getElementById('change-password-form');
    
    // Loading and error elements
    profileLoading = document.getElementById('profile-loading');
    profileError = document.getElementById('profile-error');
    profileErrorMessage = document.getElementById('profile-error-message');
    profileRetryBtn = document.getElementById('profile-retry');
}

// Set up profile event listeners
function setupProfileEventListeners() {
    // Tab switching
    document.querySelectorAll('.tab-btn').forEach(tab => {
        tab.addEventListener('click', () => {
            const tabId = tab.getAttribute('data-tab');
            switchTab(tabId);
        });
    });
    
    // Profile form submission
    if (profileForm) {
        profileForm.addEventListener('submit', (e) => {
            e.preventDefault();
            updateUserProfile();
        });
    }
    
    // Password form submission
    if (passwordForm) {
        passwordForm.addEventListener('submit', (e) => {
            e.preventDefault();
            changePassword();
        });
    }
    
    // Retry button
    if (profileRetryBtn) {
        profileRetryBtn.addEventListener('click', () => {
            loadUserProfile();
        });
    }
}

// Check if user is authenticated for profile page
function checkProfileAuth() {
    const user = JSON.parse(sessionStorage.getItem('user'));
    
    if (!user) {
        // Redirect to login if not authenticated
        window.location.href = 'home.html';
    }
}

// Load user profile data
function loadUserProfile() {
    // Show loading state
    if (profileLoading) profileLoading.classList.remove('hidden');
    if (profileError) profileError.classList.add('hidden');
    if (profileForm) profileForm.classList.add('hidden');
    
    const user = JSON.parse(sessionStorage.getItem('user'));
    if (!user) {
        showProfileError('User not authenticated');
        return;
    }
    
    // Fetch profile from server
    fetch(`api/users/${user.id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to load profile: ${response.statusText}`);
            }
            return response.json();
        })
        .then(userData => {
            populateProfileForm(userData);
            
            // Update user avatar with fetched data
            updateUserAvatar();
        })
        .catch(error => {
            console.error('Error loading profile:', error);
            
            // If API fails, use data from session storage
            populateProfileForm(user);
            
            // Still update avatar with session data
            updateUserAvatar();
        });
}

// Populate profile form with user data
function populateProfileForm(userData) {
    if (!profileForm) return;
    
    // Hide loading, show form
    if (profileLoading) profileLoading.classList.add('hidden');
    profileForm.classList.remove('hidden');
    
    // Populate form fields
    document.getElementById('profile-username').value = userData.username || '';
    document.getElementById('profile-email').value = userData.email || '';
    document.getElementById('profile-first-name').value = userData.firstName || '';
    document.getElementById('profile-last-name').value = userData.lastName || '';
    document.getElementById('profile-phone').value = userData.phone || '';
    document.getElementById('profile-address').value = userData.address || '';
    
    // Update username display in header
    const usernameDisplay = document.getElementById('username-display');
    if (usernameDisplay) {
        usernameDisplay.textContent = userData.username;
    }
    
    // Update profile name and role display
    const profileName = document.getElementById('profile-name');
    if (profileName) {
        const fullName = (userData.firstName && userData.lastName) ? 
            `${userData.firstName} ${userData.lastName}` : 
            userData.username;
        profileName.textContent = fullName;
    }
    
    const profileRole = document.getElementById('profile-role');
    if (profileRole && userData.role) {
        profileRole.textContent = userData.role.charAt(0) + userData.role.slice(1).toLowerCase();
        profileRole.className = `user-role ${userData.role.toLowerCase()}`;
    }
}

// Update user profile
function updateUserProfile() {
    // Get form data
    const formData = new FormData(profileForm);
    const user = JSON.parse(sessionStorage.getItem('user'));
    
    if (!user) {
        showProfileError('User not authenticated');
        return;
    }
    
    // Create profile data object
    const profileData = {
        id: user.id,
        username: formData.get('username'),
        email: formData.get('email'),
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        phone: formData.get('phone'),
        address: formData.get('address'),
        role: user.role
    };
    
    // Disable submit button
    const submitBtn = profileForm.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Updating...';
    }
    
    // Update profile on server
    fetch(`api/users/${user.id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(profileData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Failed to update profile: ${response.statusText}`);
        }
        return response.json();
    })
    .then(updatedData => {
        // Update session storage with new user data
        const userData = { ...user, ...updatedData };
        sessionStorage.setItem('user', JSON.stringify(userData));
        
        // Update displays with new data
        populateProfileForm(userData);
        
        // Update avatar with new user data
        updateUserAvatar();
        
        // Show success message
        showNotification('Profile Updated', 'Your profile has been updated successfully', 'success');
    })
    .catch(error => {
        console.error('Error updating profile:', error);
        showNotification('Update Failed', error.message, 'error');
    })
    .finally(() => {
        // Re-enable submit button
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Update Profile';
        }
    });
}

// Change password
function changePassword() {
    const oldPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-new-password').value;
    
    // Validate passwords
    if (newPassword !== confirmPassword) {
        showNotification('Password Error', 'New passwords do not match', 'error');
        return;
    }
    
    const user = JSON.parse(sessionStorage.getItem('user'));
    if (!user) {
        showNotification('Authentication Error', 'User not authenticated', 'error');
        return;
    }
    
    // Password data
    const passwordData = {
        oldPassword: oldPassword,
        newPassword: newPassword
    };
    
    // Disable submit button
    const submitBtn = passwordForm.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Updating...';
    }
    
    // Send password change request
    fetch(`api/users/${user.id}/password`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(passwordData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Failed to change password: ${response.statusText}`);
        }
        return response.json();
    })
    .then(() => {
        // Clear password fields
        passwordForm.reset();
        
        // Show success message
        showNotification('Password Updated', 'Your password has been changed successfully', 'success');
    })
    .catch(error => {
        console.error('Error changing password:', error);
        showNotification('Password Change Failed', error.message, 'error');
    })
    .finally(() => {
        // Re-enable submit button
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Change Password';
        }
    });
}

// Switch between tabs
function switchTab(tabId) {
    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(tab => {
        tab.classList.remove('active');
    });
    const activeTab = document.querySelector(`.tab-btn[data-tab="${tabId}"]`);
    if (activeTab) activeTab.classList.add('active');
    
    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    const activeContent = document.getElementById(`${tabId}-tab`);
    if (activeContent) activeContent.classList.add('active');
}

// Show profile error
function showProfileError(message) {
    if (profileLoading) profileLoading.classList.add('hidden');
    if (profileForm) profileForm.classList.add('hidden');
    if (profileError) profileError.classList.remove('hidden');
    if (profileErrorMessage) profileErrorMessage.textContent = message;
}

// Show notification
function showNotification(title, message, type = 'info') {
    // Create notification container if it doesn't exist
    let notificationContainer = document.querySelector('.notification-container');
    if (!notificationContainer) {
        notificationContainer = document.createElement('div');
        notificationContainer.className = 'notification-container';
        document.body.appendChild(notificationContainer);
    }
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    // Create notification content
    notification.innerHTML = `
        <div class="notification-header">
            <h3>${title}</h3>
            <span class="notification-close">&times;</span>
        </div>
        <div class="notification-body">
            <p>${message}</p>
        </div>
    `;
    
    // Add to container
    notificationContainer.appendChild(notification);
    
    // Add close button functionality
    const closeBtn = notification.querySelector('.notification-close');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            notification.remove();
        });
    }
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

// Initialize user avatar with current username
function updateUserAvatar() {
    const user = JSON.parse(sessionStorage.getItem('user'));
    if (!user) return;
    
    const avatarElement = document.getElementById('user-avatar');
    if (avatarElement) {
        avatarElement.textContent = user.username.charAt(0).toUpperCase();
        avatarElement.className = `user-avatar ${user.role.toLowerCase()}`;
    }
} 