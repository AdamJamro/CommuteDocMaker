package com.example.commutedocmaker.dataSource

class DraftEntry(val title: String, val content: String) {
    override fun toString(): String {
        return "DraftEntry(title='$title', content='$content')"
    }
    private val filePath: String = "drafts/${title}.txt"

}