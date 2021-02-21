package net.perfectdreams.dreambrisa

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.effects.CustomTotemRessurectEffect
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class DreamBrisa : KotlinPlugin(), Listener {
	companion object{
		const val PREFIX = "§8[§a☮§2§lBrisa§a☮§8] "
		private val STORIES = listOf(
			listOf(
				"MaconhaLand... The perfect land...",
				"Quer dizer, não é perfeita porque não se chama {user}Land... ¯\\_(ツ)_/¯",
				"...mas será que você é perfeito para poder falar isto?",
				"...será que alguém é perfeito?",
				"Você começa a entrar em uma crise existencial..."
			),

			listOf(
				"Você pensa sobre todas as folhas que você já... hm... comeu...",
				"Será que vale a pena mesmo *comer* uma folha de maconha?",
				"...será que o certo não seria fazer um beque bem loco?",
				"...será que você estava fazendo a coisa errada todos esses anos?",
				"Você pensa em voltar a fazer Proerd..."
			),

			listOf(
				"Sê loco menor, olha o Bob Marley aí!",
				"Mano olha o Snoop Dogg!",
				"Você tenta conversar com o nosso amigo Snoop Dogg...",
				"...mas ele desaparece em um piscar de olhos... mano, cadê o Bob Marley para trocar umas ideias marotas?",
				"E você percebe que a sua vida é uma ilusão..."
			),

			listOf(
				"Você lembra de uma coisa do seu passado na escola...",
				"\"Não fumem drogas, crianças!\"...",
				"...e você lembra da silhueta de alguém do fundo da sua memória...",
				"...ai você se lembra... a cada vez que alguém fuma maconha, um Leão da Proerd morre...",
				"...e você se lembra que você foi o Leão da Proerd na escola..."
			),

			listOf(
				"Você virou dono do SparklyPower, parabéns!",
				"Você até pensa em trocar o nome para {user}Power...",
				"...pena que você não sabe administrar um servidor, né? ¯\\_(ツ)_/¯",
				"...pessoas começam a te xingar porque você deixou o FulanoPvP123 solto mesmo após ele ter usado hack.",
				"...e você percebe que a sua vida é ser um bom e velho membro, e não um dono de um servidor famoso."
			),

			listOf(
				"Você começa a escutar uma voz...",
				"...mas ela está muito baixa para você conseguir escutar ela...",
				"...após focar um pouco na voz, você consegue ouvir...",
				"EAE, MONARK AQUI E BEM VINDOS A MAIS UM VÍDEO, DESSA VEZ GALERA...",
				"...mas ai você percebe que é só uma ilusão, o Monark ainda não voltou a gravar vídeos de Minecraft :'("
			),

			listOf(
				"Você acorda dentro de uma mansão gigante...",
				"Após andar mais um pouco na mansão, você começa a se questionar de quem é essa mansão...",
				"Afinal, você não é rico o suficiente para ter uma mansão desse tamanho...",
				"Você resolve ir até o banheiro, para fazer aquelas necessidades fisiológicas...",
				"...e você descobre que você é o Felipe Neto!"
			),

			listOf(
				"Você está com uma vontade inacreditável de falar em inglês...",
				"Mas é uma vontade tão grande, que você resolveu gritar...",
				"O P E N",
				"T H E",
				"T C H E K A"
			),

			listOf(
				"Eu sou o Dougras...",
				"Você não é o Dougras...",
				"Mas será que você é o Dougras mesmo?",
				"Quem pode dizer que você é o Dougras?",
				"Quem será o verdadeiro Dougras dessa história?"
			),

			listOf(
				"Você lembra do passado...",
				"De um servidor antigo...",
				"Que ele começava com a letra S e terminava com R...",
				"Mas também tinha um servidor que começava com D e terminava com R...",
				"Hmmm... você entra em uma crise existencial lembrando que, talvez, você nunca conseguirá rever seus amigos desses servidores..."
			),

			listOf(
				"Você encontra a 10/10 na rua...",
				"E, depois de muito tempo criando coragem você decide falar que você ama ela...",
				"Após você falar isto para ela, você espera ela responder a sua declaração de amor...",
				"E ela responde...",
				"\"Você acha mesmo que eu namoraria um maconheiro?\""
			),

			listOf(
				"Você é um dog...",
				"Mas você não é um dog comum, você é um dog especial...",
				"Tão especial, que você aparece em várias fotos que vagam pelo mundo...",
				"Sabe quem você é?",
				"O filtro de dog do Snapchat!"
			),

			listOf(
				"Você começa a escutar uma voz...",
				"\"Compre vantagens no servidor...\"",
				"Você se pergunta, \"Porque devo comprar vantagens no servidor?\"",
				"E ela responde...",
				"\"Nós precisamos de dinheiro para poder pagar a hospedagem do servidor, para que ele continue online... Ah, e você vai morrer daqui alguns segundos...\""
			),

			listOf(
				"Você começa a mexer no Photoshop...",
				"Você resolve pegar uma imagem do seu amigo para editar ela, só para brincar no Photoshop, tá ligado?",
				"Após fazer aquela edição 10/10, você resolve salvar ela no PC...",
				"No próximo dia, você resolve ir para a escola... ah não, não pode ser...",
				"As edições que você fez na imagem do seu amigo... se... se transformaram em realidade..."
			),

			listOf(
				"Eu...",
				"Tu...",
				"Nóis respeita elas...",
				"O bonde chegou, respeitador de novinhas...",
				"Legal, você tentou cantar aquele funk pesadão 10/10 mas a sua mente não deixa você falar coisas feias, fazer o que né... ¯\\_(ツ)_/¯"
			),

			listOf(
				"Você quer virar o novo Skrillex, tá ligado?",
				"Mas como será que cria umas músicas igual ao do Skrillex?",
				"Você teve uma ideia genial para criar uma música igual ao do Skrillex...",
				"Você pegou vários objetos aleatórios, jogou dentro de um liquidificador e ligou ele...",
				"Pena que você também tinha colocado você mesmo no liquidificador..."
			),

			listOf(
				"Você é o Neymar...",
				"Legal né? Neymar é daorinha, topperson...",
				"Neymar, o menino futebol...",
				"Mas, só porque você virou o Neymar, não quer dizer que você adquiriu as habilidades futebolísticas dele...",
				"E foi por isso que o Brasil perdeu na copa de 2014, parabéns por ter feito o Brasil perder, {user}. >:c"
			),

			listOf(
				"Você virou um emoji...",
				"...",
				"...",
				"...",
				"Esqueceu? Emojis não falam, só demonstram sentimentos!"
			),

			listOf(
				"Você acha mesmo que você pode ser o mascote do SparklyPower?",
				"Porque... sei lá, eu acho que não hein...",
				"Você por o acaso é interessante?",
				"Eu acho que não hein?",
				"É... provavelmente a Pantufa é melhor como a mascote do que você..."
			),

			listOf(
				"Você virou famoso, wow!",
				"Tanta gente querendo seu autográfo!",
				"Você assina um papel e dá para um fã...",
				"E o fã retruca... \"Ei, você não é o Faustão! Vamos embora gente, esse dai é só um impostor!\"",
				"E você começa a entrar na #bad..."
			)
		)
	}

	override fun softEnable() {
		super.softEnable()
		registerEvents(this)
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onInteract(e: PlayerInteractEvent) {
		val action = e.action
		val player = e.player

		if (!action.name.contains("LEFT_CLICK"))
			return

		if (player.inventory.itemInMainHand.type != Material.BROWN_MUSHROOM)
			return

		val clickedBlock = e.clickedBlock

		if (clickedBlock != null && clickedBlock.type.name.contains("SIGN")) {
			e.isCancelled = true
			return
		}

		player.inventory.itemInMainHand.amount -= 1

		if (player.hasMetadata("isBrisado")) {
			player.sendMessage("$PREFIX§cVocê já está brisado demais para comer outra folha de maconha.")
			return
		}

		if (chance(25.0)) {
			player.sendMessage("$PREFIX§cAo observar a folha mais atentamente, você percebe que ela é uma folha de grama.")
			return
		}

		e.isCancelled = true
		player.sendMessage("$PREFIX§aVocê comeu a folha de maconha...")
		CustomTotemRessurectEffect.sendCustomTotemAnimation(player, 1)
		player.setMetadata("isBrisado", FixedMetadataValue(this, true))
		player.playSound(player.location, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1F, 1F)

		val scheduler = Bukkit.getScheduler()

		scheduler.schedule(this) {
			waitFor(4 * 20)
			player.sendMessage(PREFIX + "§aVocê se sente brisado...")

			val randomStory = STORIES.random()

			player.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, ((randomStory.size * 6) * 20) + (8 * 20), 1))

			for (line in randomStory) {
				waitFor(6 * 20)
				val replacedLine = line.replace("{user}", player.displayName + "§e")
				player.sendMessage(PREFIX + "§2§k|||§a§k|||§f §e$replacedLine §2§k|||§a§k|||§f")
			}

			waitFor(4 * 20)
			player.sendMessage(PREFIX + "§2§k|||§a§k|||§f §eEntão... você §4§lmorre§e. §2§k|||§a§k|||§f")
			waitFor(4 * 20)

			player.removePotionEffect(PotionEffectType.CONFUSION)
			player.removeMetadata("isBrisado", this.plugin)
			player.sendTitle("§aLembre-se...", "§eVencedores não usam drogas!", 20, 80, 20)
			player.playSound(player.location, "perfectdreams.sfx.proerd", 1f, 1f)

			val map = ItemStack(Material.FILLED_MAP).meta<MapMeta> {
				this.mapId = if (chance(5.0)) 98 else 97
			}

			if (player.inventory.canHoldItem(map)) {
				val held = player.inventory.itemInMainHand
				player.inventory.setItemInMainHand(map)
				player.inventory.addItem(held)
			}
		}
	}
}