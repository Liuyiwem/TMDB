package com.yiwenliu.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.yiwenliu.core.model.CastMember

class CastPreviewParameterProvider : PreviewParameterProvider<List<CastMember>> {
    override val values: Sequence<List<CastMember>> =
        sequenceOf(
            listOf(
                CastMember(
                    id = 10859,
                    name = "Ryan Reynolds",
                    character = "Wade Wilson / Deadpool",
                    profileUrl = "",
                ),
                CastMember(
                    id = 6968,
                    name = "Hugh Jackman",
                    character = "Logan / Wolverine",
                    profileUrl = "",
                ),
            ),
        )
}
