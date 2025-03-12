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
        $("#loadAdminDashboard").hide(); // Hide Admin Dashboard Button for Users
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
					       <p><strong>Posted by: ${post.username}</strong></p>
					       <p>${post.content}</p>
					       <button class="btn btn-blue like-button ${post.liked ? 'liked' : ''}" 
					               data-id="${post.id}" 
					               data-likeId="${post.likeId || ''}">
					           ${post.liked ? `❤️ Liked (${post.likesCount || 0})` : `👍 Like (${post.likesCount || 0})`}
					       </button>
					       ${role === "ADMIN" ? `<button class="btn btn-danger delete-button" data-id="${post.id}">Delete</button>` : ""}
					       <div class="comment-section" id="comments-${post.id}">
					           <input type="text" class="form-control comment-input" data-id="${post.id}" placeholder="Write a comment...">
					           <button class="btn btn-green comment-button" data-id="${post.id}">Comment</button>
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

        console.log("Creating post:", title);

        $.ajax({
            type: "POST",
            url: "/api/posts/create",
            headers: { 
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            data: JSON.stringify({ title, content }),
            success: function () {
                $("#postTitle").val("");
                $("#postContent").val("");
                fetchPosts();
            },
            error: function (xhr) {
                console.error("Error creating post:", xhr);
                alert("Error creating post.");
            }
        });
    }

    function deletePost(postId) {
        console.log(`Deleting post with ID: ${postId}`);
        
        $.ajax({
            type: "DELETE",
            url: `/api/posts/${postId}`,
            headers: { 
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            success: function () {
                fetchPosts();
            },
            error: function (xhr) {
                console.error("Error deleting post:", xhr);
                alert("Error deleting post.");
            }
        });
    }

    function toggleLike(postId, button) {
        let isLiked = button.hasClass("liked");
        let likeId = button.data("likeId");

        console.log(`Toggling like for post ${postId}, isLiked: ${isLiked}`);

        let method = isLiked ? "DELETE" : "POST";
        let url = isLiked ? `/api/likes/${likeId}` : `/api/likes/post/${postId}`;

        $.ajax({
            type: method,
            url: url,
            headers: {
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            success: function (response) {
                let likesCount = response.likesCount ?? 0; 

                if (method === "POST") {
                    button.addClass("liked");
                    button.data("likeId", response.id); 
                    button.html(`❤️ Liked (${likesCount})`);
                } else {
                    button.removeClass("liked");
                    button.removeData("likeId");
                    button.html(`👍 Like (${likesCount})`);
                }
            },
            error: function (xhr) {
                console.error("Error toggling like:", xhr);
                alert("Error toggling like.");
            }
        });
    }

    function addComment(postId, commentText) {
        console.log(`Adding comment to post ${postId}: ${commentText}`);

        $.ajax({
            type: "POST",
            url: `/api/comments/create/${postId}`,
            headers: { 
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            data: JSON.stringify({ content: commentText }),
            success: function () {
                $(`.comment-input[data-id='${postId}']`).val("");  
                fetchComments();
            },
            error: function (xhr) {
                console.error("Error adding comment:", xhr);
                alert("Failed to add comment.");
            }
        });
    }

    function fetchComments() {
        console.log("Fetching comments...");

        $(".comments-list").empty(); 

        $(".post").each(function () {
            let postId = $(this).find(".comment-input").data("id");

            $.ajax({
                type: "GET",
                url: `/api/comments/${postId}`,  
                headers: { 
                    "Authorization": "Bearer " + token,
                    "Content-Type": "application/json"
                },
                success: function (response) {
                    let comments = response._embedded?.commentList;

                    if (!Array.isArray(comments)) {
                        console.error("Unexpected API response for comments:", response);
                        return;
                    }

                    let commentsContainer = $(`#comments-list-${postId}`);
                    commentsContainer.empty();

                    comments.forEach(comment => {
                        let commentHtml = `<p><strong>${comment.user.username}:</strong> ${comment.content}</p>`;
                        commentsContainer.append(commentHtml);
                    });
                },
                error: function (xhr) {
                    console.error(`Error fetching comments for post ${postId}:`, xhr);
                }
            });
        });
    }

    $(document).on("click", ".like-button", function () {
        toggleLike($(this).data("id"), $(this));
    });

    $(document).on("click", ".comment-button", function () {
        let postId = $(this).data("id");
        let commentText = $(`.comment-input[data-id='${postId}']`).val().trim();
        
        if (!commentText) {
            alert("Comment cannot be empty.");
            return;
        }

        addComment(postId, commentText);
    });

    $(document).on("click", ".delete-button", function () {
        showConfirmDialog("Are you sure you want to delete this post?", () => deletePost($(this).data("id")));
    });

    $("#createPostButton").click(createPost);

    fetchPosts();
});












