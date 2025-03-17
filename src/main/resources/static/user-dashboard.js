$(document).ready(function () {
    const token = localStorage.getItem("jwtToken");

    if (!token) {
        window.location.href = "index.html";
    }

    function getUserDataFromToken(token) {
        try {
            let payload = JSON.parse(atob(token.split(".")[1]));
            return { username: payload.username, role: payload.role };
        } catch (error) {
            console.error("Error decoding token:", error);
            window.location.href = "index.html";
        }
    }

    const { username, role } = getUserDataFromToken(token);
    $("#username").text(username);

    if (role !== "ADMIN") {
        $("#loadAdminDashboard").hide(); 
    }

    function showConfirmDialog(message, callback) {
        $("#confirmMessage").text(message);
        $("#confirmModal").removeClass("hidden");

        $("#confirmYes").off("click").on("click", function () {
            $("#confirmModal").addClass("hidden");
            callback();
        });

        $("#confirmNo").off("click").on("click", function () {
            $("#confirmModal").addClass("hidden");
        });
    }

	function fetchPosts() {
	    $("#loadingPosts").show();
	    console.log("Fetching posts from API...");

	    $.ajax({
	        type: "GET",
	        url: "/api/posts",
	        headers: { 
	            "Authorization": "Bearer " + localStorage.getItem("jwtToken"),
	            "Content-Type": "application/json"
	        },
	        success: function (response) {
	            $("#loadingPosts").hide();
	            console.log("✅ API response for posts:", response);

	            let posts = response._embedded?.hashMapList ?? [];

	            if (!Array.isArray(posts)) {
	                console.error("❌ Unexpected API response:", response);
	                alert("Error: Unexpected API response format.");
	                return;
	            }

	            $("#postsContainer").empty();

	            if (posts.length === 0) {
	                $("#postsContainer").html("<p id='noPostsMessage'>No posts available.</p>");
	            } else {
	                $("#noPostsMessage").remove();
	            }

	            

	            let postHtml = "";
	            posts.forEach(post => {
	               

	                postHtml += `
	                    <div class="post">
	                        <h5><strong>${post.title}</strong></h5>
	                        <p>${post.content}</p>
	                        <p>Posted by: ${post.username}</p>
	                        <div class="comment-section" id="comments-${post.id}">
	                            <input type="text" class="form-control comment-input" data-id="${post.id}" placeholder="Write a comment...">
	                            <button class="btn btn-green comment-button" data-id="${post.id}">Comment</button>
	                        </div>
	                        <div class="comments-list" id="comments-list-${post.id}"></div>
	                    </div>
	                `;
	            });

	            $("#postsContainer").html(postHtml);
	        },
	        error: function (xhr) {
	            $("#loadingPosts").hide();
	            console.error("❌ Error fetching posts:", xhr);
	            alert("Failed to load posts.");
	        }
	    });
	}

    function fetchComments(postId) {
        if (!postId) {
            console.error("❌ Error: postId is undefined, cannot fetch comments.");
            return;
        }

        console.log(`Fetching comments for post ${postId}...`);

        $.ajax({
            type: "GET",
            url: `/api/comments/${postId}`,
            headers: { 
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            success: function (response) {
                let comments = response._embedded?.commentList ?? [];
                let commentsList = $(`#comments-list-${postId}`);

                commentsList.empty(); 

                comments.forEach(comment => {
                    let commentHtml = `<p><strong>${comment.user.username}:</strong> ${comment.content}</p>`;
                    commentsList.append(commentHtml);
                });
            },
            error: function (xhr) {
                console.error(`❌ Error fetching comments for post ${postId}:`, xhr);
            }
        });
    }
	function addComment(postId, commentText) {
		    console.log(`Adding comment to post ${postId}:`, commentText);

		    $.ajax({
		        type: "POST",
		        url: `/api/comments/create/${postId}`,  // Ensure `postId` is in URL
		        headers: { 
		            "Authorization": "Bearer " + token,
		            "Content-Type": "application/json"
		        },
		        data: JSON.stringify({ content: commentText }), //  Use `content`, not `text`
		        success: function (response) {
		            console.log("Comment added successfully:", response);

		            //  Clear input field
		            $(".comment-input[data-id='" + postId + "']").val("");

		            //  Fetch all comments again
		            fetchComments(postId);
		        },
		        error: function (xhr) {
		            console.error("❌ Error adding comment:", xhr);
		            alert("Failed to add comment. Check console for details.");
		        }
		    });
		}



    function createPost() {
        let title = $("#postTitle").val().trim();
        let content = $("#postContent").val().trim();

        if (!title || !content) {
            alert("Title and content cannot be empty.");
            return;
        }

        $("#createPostButton").prop("disabled", true);

        $.ajax({
            type: "POST",
            url: "/api/posts/create",
            headers: { 
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            data: JSON.stringify({ title, content }),
            success: function (newPost) {
                $("#postTitle").val("");
                $("#postContent").val("");
                $("#createPostButton").prop("disabled", false);

                fetchPosts();
            },
            error: function (xhr) {
                console.error("Error creating post:", xhr);
                alert("Error creating post.");
                $("#createPostButton").prop("disabled", false);
            }
        });
    }

    $(document).off("click", ".like-button").on("click", ".like-button", function () {
        toggleLike($(this).data("id"), $(this));
    });

    $(document).off("click", ".comment-button").on("click", ".comment-button", function () {
        let postId = $(this).data("id");
        let commentText = $(`.comment-input[data-id='${postId}']`).val().trim();
        
        if (!commentText) {
            alert("Comment cannot be empty.");
            return;
        }

        addComment(postId, commentText);
    });

    $("#createPostButton").off("click").on("click", createPost);

    fetchPosts();
});














