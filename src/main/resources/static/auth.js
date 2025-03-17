$(document).ready(function () {
    function isLoggedIn() {
        return localStorage.getItem("jwtToken") !== null;
    }

    function getUserRoleFromToken(token) {
        let payload = JSON.parse(atob(token.split(".")[1]));
        return payload.role;
    }

    function showDashboard() {
        $("#authSection").hide();
        $("#dashboardSection").removeClass("hidden");
    }

    function loadContentInto(src) {
        $.get(src, function (responseText) {
            const parser = new DOMParser();
            const contentDoc = parser.parseFromString(responseText, 'text/html');
            const bodyContent = $(contentDoc).find("body").html();
            $("#content").html(bodyContent);

            $(contentDoc).find("script").each(function () {
                $.getScript($(this).attr("src"));
            });
        });
    }

    if (isLoggedIn()) {
        showDashboard();
        let userRole = getUserRoleFromToken(localStorage.getItem("jwtToken"));
        loadContentInto(userRole === "ADMIN" ? "admin-dashboard.html" : "user-dashboard.html");
    }

    // ✅ Show Registration Form
    $("#showRegister").click(function (event) {
        event.preventDefault();
        $("#loginPage").hide();
        $("#registerPage").removeClass("d-none").show();
    });

    // ✅ Show Login Form
    $("#showLogin").click(function (event) {
        event.preventDefault();
        $("#registerPage").hide();
        $("#loginPage").show();
    });

    // ✅ Handle Login
    $("#loginForm").submit(function (event) {
        event.preventDefault();

        let loginData = {
            identifier: $("#loginIdentifier").val(),
            password: $("#loginPassword").val()
        };

        $.ajax({
            type: "POST",
            url: "/api/auth/login",
            contentType: "application/json",
            data: JSON.stringify(loginData),
            success: function (response) {
                localStorage.setItem("jwtToken", response);
                let userRole = getUserRoleFromToken(response);
                alert("Login successful!");

                showDashboard();
                loadContentInto(userRole === "ADMIN" ? "admin-dashboard.html" : "user-dashboard.html");
            },
            error: function (xhr) {
                alert("Login failed: " + xhr.responseText);
            }
        });
    });

    //  Handle Registration
    $("#registerForm").submit(function (event) {
        event.preventDefault();

        let registerData = {
            username: $("#regUsername").val(),
            email: $("#regEmail").val(),
            password: $("#regPassword").val(),
            role: "USER" // Default role
        };

        $.ajax({
            type: "POST",
            url: "/api/users",
            contentType: "application/json",
            data: JSON.stringify(registerData),
            success: function () {
                alert("Registration successful! You can now log in.");
                $("#registerPage").addClass("d-none");
                $("#loginPage").show();
            },
            error: function (xhr) {
                alert("Registration failed: " + xhr.responseText);
            }
        });
    });

    // Handle Logout
    $("#logoutBtn").click(function () {
        localStorage.removeItem("jwtToken");
        window.location.href = "index.html";
    });

    //  Protect Pages
    function requireAuth() {
        if (!isLoggedIn()) {
            alert("You must be logged in!");
            window.location.href = "index.html";
        }
    }
});

