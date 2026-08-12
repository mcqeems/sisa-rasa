package com.mobile.sisarasa.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val POST_DETAIL = "post/{postId}/detail"
    const val EDIT_POST = "edit?postId={postId}&type={type}"

    fun postDetail(postId: String) = "post/$postId/detail"
    fun editPost(postId: String? = null, type: String) = "edit?postId=${postId ?: ""}&type=$type"
}