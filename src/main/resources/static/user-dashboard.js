$(document).ready(function () {
    const token = localStorage.getItem("jwtToken");

    if (!token) {
        window.location.href = "index.html";
    }

    function getUserDataFromToken(token) {
        try {
            let payload = JSON.parse(atob(token.split(".")[1]));
            return { username: payload.username };
        } catch (error) {
            console.error("Error decoding token:", error);
            window.location.href = "index.html";
        }
    }

    const { username } = getUserDataFromToken(token);
    $("#username").text(username);

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
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            success: function (response) {
                $("#loadingPosts").hide();

                console.log("API response for posts:", response);

                let posts = response._embedded?.postList;

                if (!Array.isArray(posts)) {
                    console.error("Unexpected API response. Expected an array but got:", response);
                    alert("Error: Unexpected API response.");
                    return;
                }

                let postHtml = "";
                posts.forEach(post => {
                    postHtml += `
                        <div class="post">
                            <h5>${post.title}</h5>
                            <p>${post.content}</p>
                            <button class="btn btn-success like-button" data-id="${post.id}">👍 Like</button>
                            <button class="btn btn-danger delete-button" data-id="${post.id}">Delete</button>
                            <div class="comment-section" id="comments-${post.id}">
                                <input type="text" class="form-control comment-input" data-id="${post.id}" placeholder="Write a comment...">
                                <button class="btn btn-primary comment-button" data-id="${post.id}">Comment</button>
                            </div>
                            <div class="comments-list" id="comments-list-${post.id}"></div>
                        </div>
                    `;
                });

                $("#postsContainer").html(postHtml);
                fetchComments();
            },
            error: function (xhr) {
                $("#loadingPosts").hide();
                console.error("Error fetching posts:", xhr);
                alert("Failed to load posts. Check console for details.");
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

        console.log("Creating post with title:", title, "and content:", content);

        $.ajax({
            type: "POST",
            url: "/api/posts/create",
            headers: { 
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            data: JSON.stringify({ title, content }),
            success: function (response) {
                console.log("Post created successfully:", response);
                $("#postTitle").val("");
                $("#postContent").val("");
                fetchPosts();
            },
            error: function (xhr) {
                console.error("Error creating post:", xhr);
                alert("Error creating post. Check console for details.");
            }
        });
    }

    function deletePost(postId) {
        console.log("Deleting post with ID:", postId);
        
        $.ajax({
            type: "DELETE",
            url: `/api/posts/${postId}`,
            headers: { 
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            success: function () {
                console.log("Post deleted successfully.");
                fetchPosts();
            },
            error: function (xhr) {
                console.error("Error deleting post:", xhr);
                alert("Error deleting post.");
            }
        });
    }

	function addComment(postId, commentText) {
	    console.log(`Adding comment to post ${postId}:`, commentText);

	    $.ajax({
	        type: "POST",
	        url: `/api/comments/create/${postId}`,  // ✅ Ensure `postId` is in URL
	        headers: { 
	            "Authorization": "Bearer " + token,
	            "Content-Type": "application/json"
	        },
	        data: JSON.stringify({ content: commentText }), // ✅ Use `content`, not `text`
	        success: function (response) {
	            console.log("✅ Comment added successfully:", response);

	            // ✅ Clear input field
	            $(".comment-input[data-id='" + postId + "']").val("");

	            // ✅ Fetch all comments again
	            fetchComments(postId);
	        },
	        error: function (xhr) {
	            console.error("❌ Error adding comment:", xhr);
	            alert("Failed to add comment. Check console for details.");
	        }
	    });
	}




	function fetchComments(postId) {
	    console.log(`Fetching comments for post ${postId}...`);

	    $.ajax({
	        type: "GET",
	        url: `/api/comments/${postId}`,  // ✅ Ensure this matches backend route
	        headers: { 
	            "Authorization": "Bearer " + token,
	            "Content-Type": "application/json"
	        },
	        success: function (response) {
	            console.log(`✅ Comments fetched for post ${postId}:`, response);

	            let comments = response._embedded?.commentList;
	            let commentsList = $(`#comments-list-${postId}`);

	            commentsList.empty(); // ✅ Clear previous comments

	            if (!Array.isArray(comments) || comments.length === 0) {
	                console.warn(`⚠️ No comments found for post ${postId}.`);
	                return;
	            }

	            // ✅ Append each comment under the correct post
	            comments.forEach(comment => {
	                let commentHtml = `<p><strong>${comment.user.username}:</strong> ${comment.content}</p>`;
	                commentsList.append(commentHtml);
	            });

	            console.log(`✅ Successfully displayed all ${comments.length} comments for post ${postId}.`);
	        },
	        error: function (xhr) {
	            console.error(`❌ Error fetching comments for post ${postId}:`, xhr);
	        }
	    });
	}



    $(document).on("click", ".comment-button", function () {
        let postId = $(this).data("id");
        let commentText = $(".comment-input[data-id='" + postId + "']").val().trim();
        
        if (!commentText) {
            alert("Comment cannot be empty.");
            return;
        }

        console.log(`Adding comment to post ${postId}:`, commentText);
        addComment(postId, commentText);
    });

    $(document).on("click", ".delete-button", function () {
        let postId = $(this).data("id");
        showConfirmDialog("Are you sure you want to delete this post?", function () {
            deletePost(postId);
        });
    });

    $("#createPostButton").click(createPost);
    $("#logoutButton").click(function () {
        console.log("Logging out user...");
        localStorage.removeItem("jwtToken");
        window.location.href = "index.html";
    });

    fetchPosts();
});







