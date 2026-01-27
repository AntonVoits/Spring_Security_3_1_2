let currentUserId = null;
let currentUserEditId = null;

//создаем экземпляры модальных окон
const editModal = new bootstrap.Modal(document.getElementById('editModal'));
const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));

//загружаем текущего пользователя
function loadCurrentUser() {
    fetch('/api/current-user')
        .then(response => response.json())
        .then(user => {
            const currentUserEmail = document.getElementById('currentUserEmail');
            currentUserEmail.textContent = user.email;
            currentUserEmail.dataset.userId = user.id;
            document.getElementById('currentUserRoles').textContent = user.formattedRoles;
        })
        .catch(error => {
            console.error('Error loading current user:', error);
        })
}

//загружаем всех юзеров
function loadUsers() {
    fetch('/api/users', {
        credentials: "include"
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok')
            }
            return response.json();
        })
        .then(users => {
            renderUsersTable(users);
        })
        .catch(error => {
            console.error('Error loading users:', error)
        });
}

//обрабатывает выход из системы
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

//для создания ячейки таблицы
function createCell(content) {
    const td = document.createElement('td');
    td.textContent = content;
    return td;
}

//заполнение таблицы пользователями
function renderUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = '';

    users.forEach(user => {
        const tr = document.createElement('tr');

        tr.appendChild(createCell(user.id));
        tr.appendChild(createCell(user.firstName));
        tr.appendChild(createCell(user.lastName));
        tr.appendChild(createCell(user.age));
        tr.appendChild(createCell(user.email));
        tr.appendChild(createCell(user.formattedRoles));

        const editTd = document.createElement('td');
        const editButton = document.createElement('button');
        editButton.className = 'btn btn-primary btn-sm edit-btn';
        editButton.textContent = 'Edit';
        editButton.onclick = () => openEditModal(user.id);
        editTd.appendChild(editButton);
        tr.appendChild(editTd);

        const deleteTd = document.createElement('td');
        const deleteButton = document.createElement('button');
        deleteButton.className = 'btn btn-danger btn-sm delete-btn';
        deleteButton.textContent = 'Delete';
        deleteButton.onclick = () => openDeleteModal(user.id);
        deleteTd.appendChild(deleteButton);
        tr.appendChild(deleteTd);

        tbody.appendChild(tr);
    });
}

//вкладка User и загрузка инфы туда
function loadUserInfo() {
    fetch('/api/current-user', {
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(user => {
            const tbody = document.getElementById('currentUserTableBody');
            tbody.innerHTML = `
            <tr>
                <td>${user.id}</td>
                <td>${user.firstName}</td>
                <td>${user.lastName}</td>
                <td>${user.age}</td>
                <td>${user.email}</td>
                <td>${user.formattedRoles}</td>
            </tr>
        `;
        })
        .catch(error => {
            console.error('Error loading user info:', error);
        });
}

//загрузка ролей
async function loadRoles() {
    try {
        const response = await fetch('/api/roles', {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const roles = await response.json();
        return roles;
    } catch (error) {
        console.error('Error loading roles:', error);
        return [];
    }
}

//открытие модального окна редактирования
async function openEditModal(userId) {
    try {
        const response = await fetch(`/api/users/${userId}`, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const user = await response.json();
        currentUserEditId = userId;

        const form = document.getElementById('editUserForm');
        form.querySelector('input[type="hidden"][name="id"]').value = userId;
        form.querySelector('input[type="text"][name="id"]').value = userId;
        form.querySelector('input[name="firstName"]').value = user.firstName;
        form.querySelector('input[name="lastName"]').value = user.lastName;
        form.querySelector('input[name="age"]').value = user.age;
        form.querySelector('input[name="email"]').value = user.email;

        const roleSelect = form.querySelector('select[name="roles"]');
        roleSelect.innerHTML = '';

        const roles = await loadRoles();
        roles.forEach(role => {
            const option = document.createElement('option');
            option.value = role.id;
            option.textContent = role.name.replace('ROLE_', '');

            if (user.roles.some(userRole => userRole.id === role.id)) {
                option.selected = true;
            }
            roleSelect.appendChild(option);
        });

        editModal.show();
    } catch (error) {
        console.error('Error opening edit modal:', error);
        alert('Error loading user data. Please try again.');
    }
}

//открытие модального окна удаления
async function openDeleteModal(userId) {
    try {
        const response = await fetch(`/api/users/${userId}`, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const user = await response.json();
        currentUserId = userId;

        const form = document.getElementById('deleteUserForm');
        form.querySelector('input[type="hidden"][name="id"]').value = userId;
        form.querySelector('input[type="text"][name="id"]').value = userId;
        form.querySelector('input[name="firstName"]').value = user.firstName;
        form.querySelector('input[name="lastName"]').value = user.lastName;
        form.querySelector('input[name="age"]').value = user.age;
        form.querySelector('input[name="email"]').value = user.email;

        deleteModal.show();
    } catch (error) {
        console.error('Error opening delete modal:', error);
        alert('Error loading user data. Please try again.');
    }
}

//обработка формы редактирования
async function handleEditUserSubmit(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const userId = formData.get('id');
    const currentUserId = document.getElementById('currentUserEmail').dataset.userId;

    const rolesSelect = event.target.querySelector('select[name="roles"]');
    const selectedRoles = Array.from(rolesSelect.selectedOptions).map(option => {
        return {
            id: parseInt(option.value),
            name: option.textContent
        };
    });

    const userData = {
        id: parseInt(userId),
        email: formData.get('email'),
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        age: parseInt(formData.get('age')),
        password: formData.get('password') || null,
        roles: selectedRoles
    };

    try {
        const response = await fetch(`/api/users/${userId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            throw new Error(`Error updating user: ${responseText}`);
        }

        editModal.hide();
        await loadUsers();

        if (userId === currentUserId) {
            const hasAdminRole = selectedRoles.some(role => role.name === 'ADMIN' || role.name === 'ROLE_ADMIN');

            if (!hasAdminRole) {
                window.location.href = '/user';
            } else {
                await loadUserInfo();
            }
        }
    } catch (error) {
        console.error('Error updating user:', error);
        alert('Error updating user. Please try again.');
    }
}

//обработка формы удаления
async function handleDeleteUserSubmit(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const userId = formData.get('id');

    try {
        const response = await fetch(`/api/users/${userId}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Error deleting user');
        }

        deleteModal.hide();

        await loadUsers();

        if (userId === document.getElementById('currentUserEmail').dataset.userId) {
            window.location.href = '/login-page';
        }
    } catch (error) {
        console.error('Error deleting user:', error);
        alert('Error deleting user. Please try again.');
    }
}

//обработка отправки формы добавления юзера
async function handleNewUserSubmit(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const rolesSelect = event.target.querySelector('select[name="roles"]');

    /////заполняем форму////
    const user = {
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        age: parseInt(formData.get('age')),
        email: formData.get('email'),
        password: formData.get('password'),
        roles: Array.from(rolesSelect.selectedOptions).map(option => {
            return {
                id: parseInt(option.value),
                name: option.textContent
            };
        })
    };

    try {
        const response = await fetch('/api/users', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(user)
        });

        if (!response.ok) {
            throw new Error('Error creating user');
        }

        event.target.reset();
        await loadUsers();

        const usersTableTab = document.querySelector('a[href="#usersTable"]');
        usersTableTab.click();
    } catch (error) {
        console.error('Error creating user:', error);
        alert('Error creating user. Please try again.');
    }
}

//заугрзка ролей в селект
async function loadRolesToSelect() {
    try {
        const response = await fetch('/api/roles', {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Failed to load roles');
        }

        const roles = await response.json();
        const rolesSelect = document.querySelector('#newUserForm select[name="roles"]');

        rolesSelect.innerHTML = '';

        roles.forEach(role => {
            const option = document.createElement('option');
            option.value = role.id;
            option.textContent = role.name.replace('ROLE_', '');
            rolesSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading roles:', error);
    }
}

//что загружается на страницу когда заходим на нее
document.addEventListener('DOMContentLoaded', async () => {
    try {
        // Загружаем начальные данные
        await loadCurrentUser();
        await loadUsers();
        await loadUserInfo();
        await loadRolesToSelect();

        // Добавляем обработчики форм
        const editUserForm = document.getElementById('editUserForm');
        const deleteUserForm = document.getElementById('deleteUserForm');
        const newUserForm = document.getElementById('newUserForm');

        if (editUserForm) {
            editUserForm.addEventListener('submit', handleEditUserSubmit);
        }

        if (deleteUserForm) {
            deleteUserForm.addEventListener('submit', handleDeleteUserSubmit);
        }

        if (newUserForm) {
            newUserForm.addEventListener('submit', handleNewUserSubmit);
        }
    } catch (error) {
        console.error('Error initializing page:', error);
    }
});