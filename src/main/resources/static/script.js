$(document).ready(function () {
    const token = localStorage.getItem("jwtToken");

    function getUserRoleFromToken(token) {
        try {
            let payload = JSON.parse(atob(token.split(".")[1]));
            return payload.role;
        } catch (error) {
            console.error("Invalid Token:", error);
            return null;
        }
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

            // Apply styles dynamically after content is loaded
            if (src.includes("user-dashboard")) {
                $("head").append('<link rel="stylesheet" href="user-dashboard.css" type="text/css"/>');
            } else if (src.includes("admin-dashboard")) {
                $("head").append('<link rel="stylesheet" href="admin-dashboard.css" type="text/css"/>');
            }
        });
    }

    function showDashboard() {
        $("#authSection").hide();
        $("#dashboardSection").removeClass("hidden");
    }

    if (token) {
        showDashboard();
        let userRole = getUserRoleFromToken(token);
        loadContentInto(userRole === "ADMIN" ? "admin-dashboard.html" : "user-dashboard.html");
    }

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
                showDashboard();
                let userRole = getUserRoleFromToken(response);
                loadContentInto(userRole === "ADMIN" ? "admin-dashboard.html" : "user-dashboard.html");
            },
            error: function (xhr) {
                alert("Login failed: " + xhr.responseText);
            }
        });
    });

    $("#registerForm").submit(function (event) {
        event.preventDefault();

        let username = $("#regUsername").val().trim();
        let email = $("#regEmail").val().trim();
        let password = $("#regPassword").val().trim();

        if (!username || !email || !password) {
            alert("All fields are required.");
            return;
        }

        let registerData = { username, email, password };

        $.ajax({
            type: "POST",
            url: "/api/auth/register",
            contentType: "application/json",
            data: JSON.stringify(registerData),
            success: function () {
                alert("Registration successful! You can now log in.");
                $("#registerPage").hide();
                $("#loginPage").show();
            },
            error: function (xhr) {
                alert("Registration failed: " + xhr.responseText);
            }
        });
    });

	$("#logoutBtn").off("click").on("click", function () {
	    localStorage.removeItem("jwtToken");
	    window.location.href = "index.html";
	});

    $("#showRegister").click(function () {
        $("#loginPage").hide();
        $("#registerPage").removeClass("hidden").show();
    });

    $("#showLogin").click(function () {
        $("#registerPage").hide();
        $("#loginPage").show();
    });

    $("#userDashboardBtn").click(() => loadContentInto("user-dashboard.html"));
    $("#adminDashboardBtn").click(() => loadContentInto("admin-dashboard.html"));
});







