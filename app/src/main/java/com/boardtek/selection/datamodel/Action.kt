package com.boardtek.selection.datamodel

import androidx.room.Entity

@Entity(tableName = "action")
class Action {
    var name: String
        private set
    var url: String
        private set
    var posts: Map<String, String>? = null
        private set
    var checked = false

    constructor(name: String, url: String) {
        this.name = name
        this.url = url
    }

    constructor(name: String, url: String, posts: Map<String, String>?) {
        this.name = name
        this.url = url
        this.posts = posts
    }

}