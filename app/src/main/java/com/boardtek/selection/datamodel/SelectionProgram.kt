package com.boardtek.selection.datamodel

class SelectionProgram : ArrayList<SelectionProgramItem>()

data class SelectionProgramItem(
    val data_content: List<DataContent>,
    val data_pp: List<Any>,
    val hour: String,
    val isAutoAddVersion: String,
    val isPause: String,
    val minute: String,
    val programId: String,
    val remark: String,
    val setDate: String,
    val setName: String,
    val vendorTitle: String
)

data class DataContent(
    val pressure: String,
    val pressureTime: String,
    val thermo: String,
    val thermoTime: String
)