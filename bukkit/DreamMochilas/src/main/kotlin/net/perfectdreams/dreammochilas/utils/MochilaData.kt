package net.perfectdreams.dreammochilas.utils

sealed class MochilaData(
    val name: String,
    val customModelData: Int
) {
    object Beach : MochilaData("Praia", 10)
    object Blue : MochilaData("Azul", 11)
    object Brasil : MochilaData("Brasil", 12)
    object Brocade : MochilaData("Brocade", 13)
    object Brown : MochilaData("Marrom", 14)
    object Buggy : MochilaData("Joaninha", 15)
    object Cake : MochilaData("Bolo", 16)
    object Camouflage : MochilaData("Camuflagem", 17)
    object Crown : MochilaData("Coroa", 18)
    object Deepling : MochilaData("Deepling", 19)
    object Dragon : MochilaData("Dragão", 20)
    object Energetic : MochilaData("Eletrizante", 21)
    object Expedition : MochilaData("Expedição", 22)
    object Fur : MochilaData("Pelo", 23)
    object GabiPersonagem : MochilaData("Gabriela", 24)
    object Golden : MochilaData("Dourado", 25)
    object Green : MochilaData("Verde", 26)
    object Grey : MochilaData("Cinza", 27)
    object Heart : MochilaData("Coração", 28)
    object Holding : MochilaData("Holding", 29)
    object Jewelled : MochilaData("Joías", 30)
    object Loritta : MochilaData("Loritta", 31)
    object LorittaPersonagem : MochilaData("Loritta", 32)
    object LorittaPreto : MochilaData("Loritta", 33)
    object Moderna : MochilaData("Moderno", 34)
    object Moon : MochilaData("Lua", 35)
    object Orange : MochilaData("Laranja", 36)
    object PantufaPersonagem : MochilaData("Pantufa", 37)
    object Pillow : MochilaData("Almofada", 38)
    object Pirate : MochilaData("Pirata", 39)
    object PowerPersonagem : MochilaData("Power", 40)
    object Pudim : MochilaData("Pudim", 41)
    object Purple : MochilaData("Roxo", 42)
    object Red : MochilaData("Vermelho", 43)
    object Santa : MochilaData("Papai Noel", 44)
    object Yellow : MochilaData("Amarelo", 45)
    object Rainbow : MochilaData("Arco-íris", 46)

    companion object {
        val list = listOf(
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
            Yellow
        )
    }
}