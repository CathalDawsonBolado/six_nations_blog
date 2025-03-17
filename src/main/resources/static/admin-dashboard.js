$(document).ready(function () {
    const token = localStorage.getItem("jwtToken");

    if (!token) {
        window.location.href = "index.html";
    }

    function loadUsers() {
        $("#loadingUsers").show();
        $.ajax({
            url: "/api/admin/users",
            method: "GET",
            headers: { "Authorization": "Bearer " + token },
            success: function (data) {
                $("#loadingUsers").hide();
                $("#usersTableBody").empty();
                data.forEach(user => {
                    $("#usersTableBody").append(`
                        <tr>
                            <td>${user.id}</td>
                            <td>${user.username}</td>
                            <td>${user.email}</td>
                            <td>${user.role}</td>
                            <td>${user.suspended ? "Suspended" : "Active"}</td>
                            <td>
                                <button class="btn btn-success" onclick="showConfirmDialog('Promote this user?', () => promoteUser(${user.id}))">Promote</button>
                                <button class="btn btn-warning" onclick="showConfirmDialog('Suspend this user?', () => suspendUser(${user.id}))">Suspend</button>
                                <button class="btn btn-info" onclick="showConfirmDialog('Unsuspend this user?', () => unsuspendUser(${user.id}))">Unsuspend</button>
                            </td>
                        </tr>
                    `);
                });
            },
            error: function () {
                $("#loadingUsers").hide();
                alert("Error fetching users.");
            }
        });
    }

    // ✅ Make functions globally accessible by attaching them to `window`
    window.showConfirmDialog = function (message, callback) {
        $("#confirmMessage").text(message);
        $("#confirmModal").removeClass("hidden");

        $("#confirmYes").off("click").on("click", function () {
            $("#confirmModal").addClass("hidden");
            callback();
        });

        $("#confirmNo").off("click").on("click", function () {
            $("#confirmModal").addClass("hidden");
        });
    };

    window.promoteUser = function (userId) {
        $.ajax({
            url: `/api/admin/promote/${userId}`,
            method: "PUT",
            headers: { "Authorization": "Bearer " + token },
            success: function () {
                alert("User promoted successfully.");
                loadUsers();
            },
            error: function () {
                alert("Error promoting user.");
            }
        });
    };

    window.suspendUser = function (userId) {
        $.ajax({
            url: `/api/admin/suspend/${userId}`,
            method: "PUT",
            headers: { "Authorization": "Bearer " + token },
            success: function () {
                alert("User suspended successfully.");
                loadUsers();
            },
            error: function (xhr) {
                alert(xhr.responseText);
            }
        });
    };

    window.unsuspendUser = function (userId) {
        $.ajax({
            url: `/api/admin/unsuspend/${userId}`,
            method: "PUT",
            headers: { "Authorization": "Bearer " + token },
            success: function () {
                alert("User unsuspended successfully.");
                loadUsers();
            },
            error: function () {
                alert("Error unsuspending user.");
            }
        });
    };

   

    console.log(" admin-dashboard.js loaded successfully! All functions are now globally available.");

    loadUsers();
});
 



