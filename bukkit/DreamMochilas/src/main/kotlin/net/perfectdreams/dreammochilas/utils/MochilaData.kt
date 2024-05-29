package net.perfectdreams.dreammochilas.utils

sealed class MochilaData(
    val name: String,
    val customModelData: Int
) {
    open val displayName: String? = null
    
    data object Beach : MochilaData("Praia", 10)
    data object Blue : MochilaData("Azul", 11)
    data object Brasil : MochilaData("Brasil", 12)
    data object Brocade : MochilaData("Brocade", 13)
    data object Brown : MochilaData("Marrom", 14)
    data object Buggy : MochilaData("Joaninha", 15)
    data object Cake : MochilaData("Bolo", 16)
    data object Camouflage : MochilaData("Camuflada", 17)
    data object Crown : MochilaData("Coroa", 18)
    data object Deepling : MochilaData("Oceano", 19)
    data object Dragon : MochilaData("Dragão", 20)
    data object Energetic : MochilaData("Eletrizante", 21)
    data object Expedition : MochilaData("Expedição", 22)
    data object Fur : MochilaData("Pelo", 23)
    data object GabiPersonagem : MochilaData("Gabriela Chaveiro", 24)
    data object Golden : MochilaData("Dourada", 25)
    data object Green : MochilaData("Verde", 26)
    data object Grey : MochilaData("Cinza", 27)
    data object Heart : MochilaData("Coração", 28)
    data object Holding : MochilaData("Holding", 29)
    data object Jewelled : MochilaData("Joias", 30)
    data object Loritta : MochilaData("Moletom Claro", 31)
    data object LorittaPersonagem : MochilaData("Loritta Chaveiro", 32)
    data object LorittaPreto : MochilaData("Moletom Escuro", 33)
    data object Moderna : MochilaData("Moderna", 34)
    data object Moon : MochilaData("Lua", 35)
    data object Orange : MochilaData("Laranja", 36)
    data object PantufaPersonagem : MochilaData("Pantufa Chaveiro", 37)
    data object Pillow : MochilaData("Almofada", 38)
    data object Pirate : MochilaData("Pirata", 39)
    data object PowerPersonagem : MochilaData("Power Chaveiro", 40)
    data object Pudim : MochilaData("Pudim", 41)
    data object Purple : MochilaData("Roxa", 42)
    data object Red : MochilaData("Vermelha", 43)
    data object Santa : MochilaData("Papai Noel", 44)
    data object Yellow : MochilaData("Amarela", 45)
    data object Rainbow : MochilaData("Arco-íris", 46)
    data object Bread : MochilaData("Pão", 51)
    data object Portugal : MochilaData("Portugal", 52)
    data object Pride : MochilaData("Pride", 53)
    data object Maconha : MochilaData("Maconha", 54)
    data object Coelho : MochilaData("Coelho", 55)
    data object Axolote : MochilaData("Axolote", 56)
    
    data object Birthday9WhiteArthurr : MochilaData("Nono Aniversário Branca do Arthurr", 170) {
        override val displayName = "Mochila de Aniversário do Arthurr"
    }
    data object Birthday9WhiteDelet : MochilaData("Nono Aniversário Branca do D_ELET", 171) {
        override val displayName = "Mochila de Aniversário do D_ELET"
    }
    data object Birthday9WhiteDittom : MochilaData("Nono Aniversário Branca da Stéphany", 172) {
        override val displayName = "Mochila de Aniversário da Stéphany"
    }
    data object Birthday9WhiteGabriela : MochilaData("Nono Aniversário Branca da Gabriela", 173) {
        override val displayName = "Mochila de Aniversário da Gabriela"
    }
    data object Birthday9WhiteJvgm45 : MochilaData("Nono Aniversário Branca do JvGm45", 174) {
        override val displayName = "Mochila de Aniversário do JvGm45"
    }
    data object Birthday9WhiteLoritta : MochilaData("Nono Aniversário Branca da Loritta", 175) {
        override val displayName = "Mochila de Aniversário da Loritta"
    }
    data object Birthday9WhitePantufa : MochilaData("Nono Aniversário Branca da Pantufa", 176) {
        override val displayName = "Mochila de Aniversário da Pantufa"
    }
    data object Birthday9WhitePaum : MochilaData("Nono Aniversário Branca da Paum", 177) {
        override val displayName = "Mochila de Aniversário da Paum"
    }
    data object Birthday9WhitePower : MochilaData("Nono Aniversário Branca do Power", 178) {
        override val displayName = "Mochila de Aniversário do Power"
    }
    data object Birthday9WhiteTbag : MochilaData("Nono Aniversário Branca do TBAG", 179) {
        override val displayName = "Mochila de Aniversário do TBAG"
    }
    data object Birthday9WhiteVegeta : MochilaData("Nono Aniversário Branca do Vegeta", 180) {
        override val displayName = "Mochila de Aniversário do Vegeta"
    }
    data object Birthday9WhiteZum : MochilaData("Nono Aniversário Branca do Zum", 181) {
        override val displayName = "Mochila de Aniversário do Zum"
    }
    data object Birthday9BlackArthurr : MochilaData("Nono Aniversário Preta do Arthurr", 182) {
        override val displayName = "Mochila de Aniversário do Arthurr"
    }
    data object Birthday9BlackDelet : MochilaData("Nono Aniversário Preta do D_ELET", 183) {
        override val displayName = "Mochila de Aniversário do D_ELET"
    }
    data object Birthday9BlackDittom : MochilaData("Nono Aniversário Preta da Stéphany", 184) {
        override val displayName = "Mochila de Aniversário da Stéphany"
    }
    data object Birthday9BlackGabriela : MochilaData("Nono Aniversário Preta da Gabriela", 185) {
        override val displayName = "Mochila de Aniversário da Gabriela"
    }
    data object Birthday9BlackJvgm45 : MochilaData("Nono Aniversário Preta do JvGm45", 186) {
        override val displayName = "Mochila de Aniversário do JvGm45"
    }
    data object Birthday9BlackLoritta : MochilaData("Nono Aniversário Preta da Loritta", 187) {
        override val displayName = "Mochila de Aniversário da Loritta"
    }
    data object Birthday9BlackPantufa : MochilaData("Nono Aniversário Preta da Pantufa", 188) {
        override val displayName = "Mochila de Aniversário da Pantufa"
    }
    data object Birthday9BlackPaum : MochilaData("Nono Aniversário Preta da Paum", 189) {
        override val displayName = "Mochila de Aniversário da Paum"
    }
    data object Birthday9BlackPower : MochilaData("Nono Aniversário Preta do Power", 190) {
        override val displayName = "Mochila de Aniversário do Power"
    }
    data object Birthday9BlackTbag : MochilaData("Nono Aniversário Preta do TBAG", 191) {
        override val displayName = "Mochila de Aniversário do TBAG"
    }
    data object Birthday9BlackVegeta : MochilaData("Nono Aniversário Preta do Vegeta", 192) {
        override val displayName = "Mochila de Aniversário do Vegeta"
    }
    data object Birthday9BlackZum : MochilaData("Nono Aniversário Preta do Zum", 193) {
        override val displayName = "Mochila de Aniversário do Zum"
    }
    data object StickerRarityCommon : MochilaData("Figurinha Comum", 198)
    data object StickerRarityUncommon : MochilaData("Figurinha Incomum", 199)
    data object StickerRarityRare : MochilaData("Figurinha Rara", 200)
    data object StickerRarityEpic : MochilaData("Figurinha Épica", 201)
    data object StickerRarityLegendary : MochilaData("Figurinha Lendária", 202)
    data object StickerRaritySpecial : MochilaData("Figurinha Especial", 203)

    companion object {
        val list: List<MochilaData>
            get() = listOf(
                Beach,
                Blue,
                Brasil,
                Brocade,
                Brown,
                Buggy,
                Cake,
                Camouflage,
                Crown,
                Deepling,
                Dragon,
                Energetic,
                Expedition,
                Fur,
                GabiPersonagem,
                Golden,
                Green,
                Grey,
                Heart,
                Holding,
                Jewelled,
                Loritta,
                LorittaPersonagem,
                LorittaPreto,
                Moderna,
                Moon,
                Orange,
                PantufaPersonagem,
                Pillow,
                Pirate,
                PowerPersonagem,
                Pudim,
                Purple,
                Red,
                Santa,
                Yellow,
                Rainbow,
                Bread,
                Portugal,
                Pride,
                Maconha,
                Coelho,
                Axolote,
                Birthday9WhiteArthurr,
                Birthday9WhiteDelet,
                Birthday9WhiteDittom,
                Birthday9WhiteGabriela,
                Birthday9WhiteJvgm45,
                Birthday9WhiteLoritta,
                Birthday9WhitePantufa,
                Birthday9WhitePaum,
                Birthday9WhitePower,
                Birthday9WhiteTbag,
                Birthday9WhiteVegeta,
                Birthday9WhiteZum,
                Birthday9BlackArthurr,
                Birthday9BlackDelet,
                Birthday9BlackDittom,
                Birthday9BlackGabriela,
                Birthday9BlackJvgm45,
                Birthday9BlackLoritta,
                Birthday9BlackPantufa,
                Birthday9BlackPaum,
                Birthday9BlackPower,
                Birthday9BlackTbag,
                Birthday9BlackVegeta,
                Birthday9BlackZum,
                StickerRarityCommon,
                StickerRarityUncommon,
                StickerRarityRare,
                StickerRarityEpic,
                StickerRarityLegendary,
                StickerRaritySpecial
            )

        val birthday9: List<MochilaData>
            get() = listOf(
                Birthday9WhiteArthurr,
                Birthday9WhiteDelet,
                Birthday9WhiteDittom,
                Birthday9WhiteGabriela,
                Birthday9WhiteJvgm45,
                Birthday9WhiteLoritta,
                Birthday9WhitePantufa,
                Birthday9WhitePaum,
                Birthday9WhitePower,
                Birthday9WhiteTbag,
                Birthday9WhiteVegeta,
                Birthday9WhiteZum,
                Birthday9BlackArthurr,
                Birthday9BlackDelet,
                Birthday9BlackDittom,
                Birthday9BlackGabriela,
                Birthday9BlackJvgm45,
                Birthday9BlackLoritta,
                Birthday9BlackPantufa,
                Birthday9BlackPaum,
                Birthday9BlackPower,
                Birthday9BlackTbag,
                Birthday9BlackVegeta,
                Birthday9BlackZum,
            )

        val customModelDataIds: Set<Int>
            get() = MochilaData.list.map { it.customModelData }.toSet()
    }
}