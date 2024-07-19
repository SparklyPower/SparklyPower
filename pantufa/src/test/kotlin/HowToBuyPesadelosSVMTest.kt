import kotlin.test.Test
import kotlin.test.assertEquals

class HowToBuyPesadelosSVMTest : SVMBaseTest(
    "svm-how-to-buy-pesadelos",
    listOf(
        "como compra pesadelos?",
        "qual é o link da loja de pesadelos do sparkly?"
    ),
    listOf(
        "como compra itens na loja?"
    )
)