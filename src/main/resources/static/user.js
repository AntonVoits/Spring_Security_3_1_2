document.getElementById('logoutForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
        const response = await fetch('/logout', {
            method: 'POST',
            credentials: 'include'
        });

        if (response.ok) {
            window.location.href = '/login-page';
        }
    } catch (error) {
        console.error('Error logging out:', error);
    }
});

function loadCurrentUser() {
    fetch('/api/current-user')
        .then(response => response.json())
        .then(user => {
            const currentUserEmail = document.getElementById('currentUserEmail');
            currentUserEmail.textContent = user.email;
            currentUserEmail.dataset.userId = user.id;
            document.getElementById('currentUserRoles').textContent = user.formattedRoles;
            renderUsersTable(user);
        })
        .catch(error => {
            console.error('Error loading current user:', error);
        })
}

function createCell(content) {
    const td = document.createElement('td');
    td.textContent = content;
    return td;
}

function renderUsersTable(user) {
    const tbody = document.getElementById('usersTableBodyById');
    tbody.innerHTML = '';

    const tr = document.createElement('tr');
    tr.appendChild(createCell(user.id));
    tr.appendChild(createCell(user.firstName));
    tr.appendChild(createCell(user.lastName));
    tr.appendChild(createCell(user.age));
    tr.appendChild(createCell(user.email));
    tr.appendChild(createCell(user.formattedRoles));
    tbody.appendChild(tr);
}

document.addEventListener('DOMContentLoaded', async () => {
    try {
        await loadCurrentUser();
    } catch (error) {
        console.error('Error initializing page:', error);
    }
});