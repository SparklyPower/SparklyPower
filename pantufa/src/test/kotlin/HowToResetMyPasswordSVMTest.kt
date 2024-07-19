import kotlin.test.Test
import kotlin.test.assertEquals

class HowToResetMyPasswordSVMTest : SVMBaseTest(
    "svm-how-to-reset-my-password",
    listOf(
        "como altera a minha senha?",
        "oi gente como altera a minha senha?",
        "como reseta a minha senha?",
        "oi gente eu esqueci a minha senha",
        "oi gente como recupera a minha senha?",
        "como recupera a senha",
        "como recupera a senha do sparkly?",
        "eu perdi a senha da minha conta do SparklyPower",
        "e não me lembro minha senha",
        "oiii pfv você pode falar como muda a senha da minha conta?",
        "nao lembro a senha"
    ),
    listOf(
        "Olá boa tarde Atualmente eu jogo no sparkly pelo mine original pelo pc. Eventualmente comprei o mine pelo Iphone, eu consiguirei a jogar no sparkly em ambos?? através da minha conta normalmente.",
        "resetei a sua senha",
        "resetei sua senha",
        "oi galera, eu esqueci como vê as minhas homes no servidor"
    )
)