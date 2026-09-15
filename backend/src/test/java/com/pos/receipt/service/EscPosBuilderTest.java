package com.pos.receipt.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class EscPosBuilderTest {

    @Test
    void init_emitsEscAtCommand() {
        byte[] bytes = new EscPosBuilder().init().build();

        assertThat(bytes).containsExactly(0x1B, 0x40);
    }

    @Test
    void line_appendsTextFollowedByNewline() {
        byte[] bytes = new EscPosBuilder().line("Halo").build();

        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("Halo\n");
    }

    @Test
    void twoColumns_padsWithSpacesSoRightTextAlignsToGivenWidth() {
        byte[] bytes = new EscPosBuilder().twoColumns("Total", "Rp1000", 16).build();

        String line = new String(bytes, StandardCharsets.UTF_8);
        assertThat(line).isEqualTo("Total     Rp1000\n");
        assertThat(line).hasSize(17); // 16 kolom + newline
    }

    @Test
    void twoColumns_fallsBackToSingleSpace_whenTextsTogetherExceedWidth() {
        byte[] bytes = new EscPosBuilder().twoColumns("Nama Produk Sangat Panjang", "Rp1000", 10).build();

        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("Nama Produk Sangat Panjang Rp1000\n");
    }

    @Test
    void divider_repeatsDashCharacterToGivenWidth() {
        byte[] bytes = new EscPosBuilder().divider(8).build();

        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("--------\n");
    }

    @Test
    void cut_endsWithPartialCutCommandAfterFeedLines() {
        byte[] bytes = new EscPosBuilder().cut().build();

        assertThat(bytes).endsWith(new byte[] {0x1D, 0x56, 0x01});
        // 3 feed lines ('\n') precede the cut command.
        assertThat(bytes).containsSequence('\n', '\n', '\n', 0x1D, 0x56, 0x01);
    }
}
