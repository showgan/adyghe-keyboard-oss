package com.adyghe.keyboard.core.input

import org.junit.Assert.assertEquals
import org.junit.Test

class InputConnectionControllerDeleteBackwardTest {

    @Test
    fun deleteBackward_bmpLetter_deletesOneUnit() {
        val editor = RecordingEditorConnection(text = "ab", cursor = 2)
        val controller = controller(editor)

        controller.deleteBackward()

        assertEquals("a", editor.text)
        assertEquals(1, editor.cursor)
        assertEquals(listOf("deleteSurrounding(1,0)"), editor.editorOps())
    }

    @Test
    fun deleteBackward_supplementaryEmoji_deletesWholeSurrogatePair() {
        val smile = "😀"
        val editor = RecordingEditorConnection(text = "a$smile", cursor = 1 + smile.length)
        val controller = controller(editor)

        controller.deleteBackward()

        assertEquals("a", editor.text)
        assertEquals(1, editor.cursor)
        assertEquals(listOf("deleteSurrounding(${smile.length},0)"), editor.editorOps())
    }

    @Test
    fun deleteBackward_skinToneSequence_deletesWholeCluster() {
        val thumbs = "👍🏽"
        val editor = RecordingEditorConnection(text = "hi$thumbs", cursor = 2 + thumbs.length)
        val controller = controller(editor)

        controller.deleteBackward()

        assertEquals("hi", editor.text)
        assertEquals(2, editor.cursor)
        assertEquals(listOf("deleteSurrounding(${thumbs.length},0)"), editor.editorOps())
    }

    @Test
    fun deleteBackward_zwjSequence_deletesWholeCluster() {
        val family = "👨‍👩‍👧"
        val editor = RecordingEditorConnection(text = family, cursor = family.length)
        val controller = controller(editor)

        controller.deleteBackward()

        assertEquals("", editor.text)
        assertEquals(0, editor.cursor)
        assertEquals(listOf("deleteSurrounding(${family.length},0)"), editor.editorOps())
    }

    @Test
    fun deleteBackward_emptyBeforeCursor_fallsBackToOneUnit() {
        val editor = RecordingEditorConnection(text = "ab", cursor = 2)
        editor.staleTextBefore = ""
        val controller = controller(editor)

        controller.deleteBackward()

        assertEquals("a", editor.text)
        assertEquals(1, editor.cursor)
        assertEquals(listOf("deleteSurrounding(1,0)"), editor.editorOps())
    }

    private fun controller(editor: RecordingEditorConnection): InputConnectionController {
        return InputConnectionController().apply { setInputConnection(editor) }
    }
}
