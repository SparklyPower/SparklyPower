import kotlin.test.Test
import kotlin.test.assertEquals

class HowToBuyPesadelosSVMTest : SVMBaseTest(
    "svm-how-to-buy-pesadelos",
    listOf(
        "como ganha pesadelos?",
        "como compra pesadelos?",
        "como consegue pesadelos?",
        "qual é o link da loja de pesadelos do sparkly?",
        "oi gente queria comprar pesadelos"
    ),
    listOf(
        "como compra itens na loja?",
        "eu enviei pesadelos para a pessoa errada como consigo de volta?",
        "gente eu enviei pesadelos para a pessoa errada como eu consigo eles de volta?"
    )
)