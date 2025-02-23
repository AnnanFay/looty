package looty
package model

import looty.poeapi.PoeTypes.AnyItem
import looty.model.WeaponTypes.WeaponType


//////////////////////////////////////////////////////////////
// Copyright (c) 2013 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 12/14/13 1:06 PM
//////////////////////////////////////////////////////////////


case class MinMaxDamage(var min: Double, var max: Double) {
  def avg = (min + max) / 2.0
  def +=(min: Double, max: Double) {
    this.min += min
    this.max += max
  }
  def set(that: MinMaxDamage) {
    this.min = that.min
    this.max = that.max
  }
}

//object ComputedItem {
//  implicit class ComputedItemExtensions(val item : ComputedItem) extends AnyVal {
//
//  }
//
//  def apply(item : AnyItem) : ComputedItem = {
//    ???
//  }
//}

class ComputedItem(val item: AnyItem, val containerId: LootContainerId, val locationName: String) {
  lazy val maxLinks: Int = item.sockets.toOption.map(_.toList.map(_.group).groupBy(x => x).map(_._2.size).maxOptI.getOrElse(0)).getOrElse(0)


  object Scores {
    lazy val default: ItemScore = ItemScorer(ComputedItem.this).getOrElse(ItemScore(Nil, 0))
    lazy val custom : ItemScore = ItemScorer(ComputedItem.this).getOrElse(ItemScore(Nil, 0))
    lazy val custom2: HighScore = HighScorer(ComputedItem.this).getOrElse(HighScore(Nil, 0))
  }

  def maxResist = plusTo.resistance.all.max
  def magicFind = increased.quantityOfItemsFound + increased.rarityOfItemsFound

  def isEquippable = !item.isGem &&
    !item.isCurrency &&
    !item.isMap &&
    !item.isQuest &&
    !item.isFragment &&
    !item.isSplinter &&
    !item.isHideoutItem &&
    !item.isJewel &&
    !item.isDivinationCard &&
    !item.isProphecy &&
    !item.isLeaguestone &&
    !item.isOrgan

  lazy val displayName = {
    var n = item.getName
    if (n.nullSafe.isEmpty || n.isEmpty) n = item.typeLine
    n
  }



  def forumLocationName = {
    //[linkItem location="Stash4" league="Rampage" x="0" y="0"]
    //[linkItem location="MainInventory" character="frostlarr" x="0" y="2"]
    for (x <- item.x.toOption; y <- item.y.toOption) yield {
      containerId match {
        case CharInvId(char) =>
          val slot = item.inventoryId.toOption.getOrElse("???")
          s"""[linkItem location="$slot" character="$char" x="$x" y="$y"]"""
        case StashTabIdx(idx) =>
          val league = item.league
          s"""[linkItem location="Stash${idx + 1}" league="$league" x="$x" y="$y"]"""
      }
    }
  }

  def locationId = item.locationId.toOption.getOrElse {
    console.error("Unable to find a location id", this.asJsAny, item)
    sys.error("Unable to find a location id")
  }

  //This location includes coordinates
  lazy val locAndCoords = {
    val l = Some(locationName)
    val s = containerId match {
      case StashTabIdx(i) => Some("s:" + i)
      case _ => None
    }
    val x = item.x.toOption.map(_ + 1).map("x:" + _)
    val y = item.y.toOption.map(_ + 1).map("y:" + _)
    List(l, s, x, y).flatten.mkString(" ")
  }

  lazy val typeName = {
    if (slots.isAmulet) "Amulet"
    else if (slots.isRing) "Ring"
    else if (slots.isHelmet) "Arm Helmet"
    else if (slots.isChest) "Arm Chest"
    else if (slots.isGloves) "Arm Gloves"
    else if (slots.isBoots) "Arm Boots"
    else if (slots.isBelt) "Arm Belt"
    else if (slots.isShield) "Arm Shield"
    else if (slots.isQuiver) "Quiver"
    else if (slots.isFlask) "Flask"
    else if (slots.isWeapon) "Wep " + properties.weaponType.toShortString
    else if (item.isHideoutItem) "Hideout"
    else if (item.isSupportGem) "Support Gem"
    else if (item.isSkillGem) "Skill Gem"
    else if (item.isMap) "Map"
    else if (item.isFragment) "Fragment"
    else if (item.isSplinter) "Splinter"
    else if (item.isQuest) "QuestItem"
    else if (item.isClusterJewel) "Cluster Jewel"    
    else if (item.isJewel && !item.isAbyssJewel) "Jewel"
    else if (item.isAbyssJewel) "Abyss Jewel"
    else if (item.isDivinationCard) "DivCard"
    else if (item.isLeaguestone) "Leaguestone"
    else if (item.isProphecy) "SealedProphecy"
    else if (item.isOrgan) "Organ"
    else if (item.isHirelingE) "Heist Tool"
    else if (item.isBlueprint) "Blueprint"
    else if (item.isContract) "Contract"
    else if (item.isLogbook) "Logbook"
    else if (item.isResonator) "Resonator"
    // moving it as the last one to not include possible fragments catches as currency
    // which are recognized via description text
    // thanks GGG for lack of proper handling it :/
    else if (item.isCurrency) "Currency"
    else "UNKNOWN"
  }

  object chanceTo {
    var blockSpellDamage               = 0.0
    var dodgeSpellHits                 = 0.0
    var suppressSpellDamage            = 0.0
  }

  object increased {
    var attributes                     = 0.0
    var dexterity                      = 0.0
    var intellect                      = 0.0
    var strength                       = 0.0

    var globalDamage                   = 0.0
    var areaDamage                     = 0.0
    var attackDamage                   = 0.0
    var meleeDamage                    = 0.0
    var areaOfEffects                  = 0.0

    val damage                         = Elements mutable 0.0
    var bleedingDamage                 = 0.0
    var burningDamage                  = 0.0
    var elementalAttackDamage          = 0.0
    var stunDurationOnEnemies          = 0.0
    var chillDurationOnEnemies         = 0.0
    var attackSpeed                    = 0.0
    var globalCriticalStrikeMultiplier = 0.0
    var globalCriticalStrikeChance     = 0.0
    var criticalStrikeChance           = 0.0
    var criticalStrikeChanceForSpells  = 0.0
    var globalArmour                   = 0.0
    var globalEvasionRating            = 0.0
    var localArmour                    = 0.0
    var localEvasionRating             = 0.0
    var localEnergyShield              = 0.0
    var maximumEnergyShield            = 0.0
    var maximumLife                    = 0.0
    var maximumMana                    = 0.0
    var quantityOfItemsFound           = 0.0
    var rarityOfItemsFound             = 0.0
    var movementSpeed                  = 0.0
    var blockAndStunRecovery           = 0.0
    var spellDamage                    = 0.0
    var manaRegenerationRate           = 0.0
    var elementalDamageWithWeapons     = 0.0
    var lightRadius                    = 0.0
    var castSpeed                      = 0.0
    var projectileSpeed                = 0.0
    var accuracyRating                 = 0.0
    var blockRecovery                  = 0.0
    var elementalDamage                = 0.0
//    var dotDamage                     = 0.0 //TODO add mods increased
//    var maximumMana                    = 0.0 //TODO add mods increased
  }

  object increasedSpell {
    def elemental = increased.spellDamage + increased.elementalDamage
    val elements = new Elements[Double] {
      override def physical: Double = increased.spellDamage + increased.damage.physical
      override def fire: Double = increased.spellDamage + increased.damage.fire
      override def cold: Double = increased.spellDamage + increased.damage.cold
      override def chaos: Double = increased.spellDamage + increased.damage.lightning
      override def lightning: Double = increased.spellDamage + increased.damage.chaos
    }
  }

  object reduced {
    var attributeRequirements = 0.0
    var enemyStunThreshold    = 0.0
//    var costOfSkills          = 0.0 //TODO add mods reduced
//    var manaReserved          = 0.0 //TODO add mods reduced
  }

  var sockets: List[List[String]] = Nil
  lazy val socketColors      = {
    if (sockets.nonEmpty) {
      sockets.map(_.mkString("-")).mkString(" ")
    } else if (item.isGem) {
      requirements.attribute.toMap.toList.filter(_._2 > 0).maxByOpt(_._2).map(_._1.color) match {
        case Some(color) => color.toOneLetter
        case None => "?"
      }
    } else {
      ""
    }

  }
  lazy val socketCnt   : Int = sockets.map(_.size).sum
  lazy val maxLink           = sockets.map(_.size).maxOpt.getOrElse(0)
  lazy val propLevel   : Int = item.getLevel.getOrElse(0)
  lazy val mapLevel    : Int = item.getMapLevel.getOrElse(0)
  lazy val countInStack: Int = item.getCountInStack.getOrElse(0)
  //Sockets in cluster jewel
  val clusterJewelSockets: Double = passiveSkill.socketCount

  lazy val misc: Double = {
    if (countInStack > 0) countInStack
    else if (socketCnt > 0) socketCnt
    else if (propLevel > 0) propLevel
    else if (mapLevel > 0) mapLevel
    else if (clusterJewelSockets > 0) clusterJewelSockets
    else 0.0
  }

  object requirements {
    var level     = 0.0
    var attribute = Attributes.mutable(0.0)
  }

  val damages         = Elements of MinMaxDamage(0, 0)
  val damagesWithBows = Elements of MinMaxDamage(0, 0)
  val addDamagesToSpells   = Elements of MinMaxDamage(0, 0)

  def addsDamageToSpellsTotal = addDamagesToSpells.all.map(_.avg).sum

  object plusTo {
    val attribute  = Attributes mutable 0.0
    val resistance = Elements mutable 0.0
    def totalResistance = resistance.all.sum
    def maxResistance = resistance.all.max
    val resistanceCap = Elements mutable 0.0
    def totalResistanceCap = resistanceCap.all.sum
    val lifeAndMana = LifeAndMana mutable 0.0
    lazy val lifeAndManaWithStrInt = lifeAndMana.map2(_ + plusTo.attribute.strength * .5, _ + plusTo.attribute.intelligence * .5)
    var accuracyRating = 0.0
    lazy val accuracyRatingWithDex = accuracyRating + plusTo.attribute.dexterity * 2
    var evasionRating = 0.0
    var armour        = 0.0
    var energyShield  = 0.0
  }

  object regenPerSecond {
    var flat = LifeAndMana mutable 0.0 // Regenerate 1.2 Life per second  or  Regenerate 1.2 Mana per second
    var percent = LifeAndMana mutable 0.0 // Regenerate 2.0% Life per second  or  Regenerate 2.0% Mana per second
  }

  object leech {var physical = LifeAndMana mutable 0.0}
  object onKill {var lifeAndMana = LifeAndMana mutable 0.0}
  object recoverOnKill {
    var lifeAndMana = LifeAndMana mutable 0.0
    var energyShield = 0.0
  }
  object onAttackHit {var lifeAndMana = LifeAndMana mutable 0.0}

  object socketedGemLevel {
    val element   = Elements mutable 0.0
    val attribute = Attributes mutable 0.0
    var melee     = 0.0
    var minion    = 0.0
    var trapOrMine = 0.0
    var bow       = 0.0
    var any       = 0.0
    var support   = 0.0
    def addToAll(n: Double) = {
      Elements.all.foreach(element +=(_, n))
      Attributes.all.foreach(attribute +=(_, n))
      melee += n
      minion += n
      trapOrMine += n
      bow += n
      any += n
      support += n
    }
    def max = (List(melee, minion, trapOrMine, support, bow) ::: attribute.all ::: element.all).max
  }

  object allGemLevel {
    val element   = Elements mutable 0.0
    val attribute = Attributes mutable 0.0
    var minion        = 0.0
    var any           = 0.0
    def addToAll(n: Double) = {
      Elements.all.foreach(element +=(_, n))
      Attributes.all.foreach(attribute +=(_, n))
      minion += n
      any += n
    }
    def max = (List(minion) ::: attribute.all ::: element.all).max
  }
  object allSpellGemLevel {
    val element   = Elements mutable 0.0
    var any     = 0.0

    def addToAll(n: Double) = {
      Elements.all.foreach(element +=(_, n))
      any += n
    }
  }

  object total {
    lazy val dps       = perElementDps.all.sum
    lazy val eDps      = perElementDps.fire + perElementDps.cold + perElementDps.lightning
    lazy val avgDamage = properties.damages.all.map(_.avg).sum

    lazy val perElementDps = Elements calculatedWith { element =>
      if (slots.isWeapon) {
        properties.damages(element).avg * properties.attacksPerSecond
      } else {
        damages(element).avg
      }
    }

    lazy val avgDamages = Elements calculatedWith { element =>
      if (slots.isWeapon) {
        properties.damages(element).avg
      } else {
        damages(element).avg
      }
    }
    def roundingDexToEvasionRating(dex:Double): Double = {
      //Every 5 points of dexterity provide 1% increased Evasion Rating. Non-multiples of 5 will be rounded up to the nearest multiple of 5 (e.g. 142 dexterity will be rounded to 145)
      if (dex % 5 > 0) math.ceil(dex / 5) else (dex / 5)
    }

    def armour = properties.armour.oIf(_ == 0.0, x => plusTo.armour, x => x)
    def evasion = properties.evasion.oIf(_ == 0.0, x => plusTo.evasionRating, x => x)
    def globalEvasionRating = increased.globalEvasionRating + roundingDexToEvasionRating(plusTo.attribute.dexterity)
    def energyShield = properties.energyShield.oIf(_ == 0.0, x => plusTo.energyShield, x => x)
    def globalEnergyShield = increased.maximumEnergyShield + math.ceil(plusTo.attribute.intelligence * .2)
    def critChance = (100 + increased.globalCriticalStrikeChance) / 100.0 * properties.criticalStrikeChance
  }

  object slots {
    def is1H: Boolean = properties.weaponType.is1H
    def is2H: Boolean = properties.weaponType.is2H
    def isWeapon: Boolean = properties.weaponType.isWeapon
    def isFlask = item.isFlask
    var isSpiritShield = false
    var isShield       = false

    var isHelmet = false
    var isChest  = false
    var isGloves = false
    var isAmulet = false
    var isRing   = false
    var isBelt   = false
    var isBoots  = false
    var isQuiver = false
    var isHeistMemberItem = false
  }


  object properties {
    var weaponType: WeaponType = WeaponTypes.NoWeaponType
    var armour                 = 0.0
    var energyShield           = 0.0
    var evasion                = 0.0
    val damages                = Elements of MinMaxDamage(0, 0)
    var quality                = 0.0
    var criticalStrikeChance   = 0.0
    var attacksPerSecond       = 0.0
    var chanceToBlock          = 0.0
    var weaponRange            = 0.0
    var stackSize              = 0.0
    var abyss                  = "Abyss"
    var limitedTo              = 0.0
	  var radius                 = ""
  }

  var reflectsPhysicalDamageToAttackers = 0.0
  var blockChance                       = 0.0
  var numExplicitModSockets             = 0.0
  var minusToManaCostOfSkills           = 0.0
  var arrowPierceChance                 = 0.0
  var bleedingChance                    = 0.0
  var freezeChance                      = 0.0
  var shockChance                       = 0.0

  object flask {
    object increased {
      var lifeRecoveryRate   = 0.0
      var effectDuration     = 0.0
      var manaRecoveryRate   = 0.0
      var flaskRecoverySpeed = 0.0
      var chargesGained      = 0.0
      var chargeRecovery     = 0.0
      var stunRecovery       = 0.0
      var recoverySpeed      = 0.0
      var amountRecovered    = 0.0
      var recoveryOnLowLife  = 0.0
      var lifeRecovered      = 0.0
      var manaRecovered      = 0.0
      var armour             = 0.0
      var evasion            = 0.0
      var movementSpeed      = 0.0
      //var increasedEffect  = 0.0 //TODO add flask mod

    }

    object reduced {
      var amountRecovered  = 0.0
      var recoverySpeed    = 0.0
      var flaskChargesUsed = 0.0
      //var duration         = 0.0 //TODO add flask mod
    }

    var extraCharges                 = 0.0
    var amountAppliedInstantly       = 0.0
    var chargesOnCriticalStrikeTaken = 0.0
    var chargesOnCriticalStrikeGiven = 0.0
    var lifeFromMana                 = 0.0
    var manaFromLife                 = 0.0
    var immunityTime                 = 0.0

    var additionalResistances = 0.0
    var lifeRecoveryToMinions = 0.0


    var removesFrozenAndChilled = false
    var removesShocked          = false
    var removesBurning          = false
    var removesBleeding         = false
    var removesCurses           = false
    var removesPoison           = false
    var removesHinderAndMaim    = false

    var knockback              = false
    var instantRecovery        = false
    var instantRecoveryLowLife = false
  }

  object minions {
    var damage                        = 0.0
    var areaOfEffects                 = 0.0
    var attackSpeed                   = 0.0
    var castSpeed                     = 0.0
    var maximumLife                   = 0.0
    var movementSpeed                 = 0.0
    var eleResist                     = 0.0
    var dblDamage                     = 0.0
    var accuracyRating                = 0.0
    var reducedReflectedDamage        = 0.0
    var increasedMinionDuration       = 0.0
    //on abyss jewels
    var increasedDamageWhenSkillUsed  = 0.0
  }

  object traps {
    var damage                  = 0.0
    var throwingSpeed           = 0.0
    var reducedDuration         = 0.0
  }

  object mines {
    var damage                  = 0.0
    var throwingSpeed           = 0.0
    var reducedDuration         = 0.0
  }

  object totems {
    var damage                  = 0.0
    var life                    = 0.0
    var allElemResists          = 0.0
  }

  //field for mods which we could not/will not parse
  //so we could search through column with them anyway
  object  notParsedYet {
    var name = ""
    //count - maybe?
  }

  object socketedGems {
    var name = ""
  }

	object skill {
		var name = ""
		var level = 0.0
	}
  object passiveSkill {
    //# Added Passive Skill is Widespread Destruction
    //# Added Passive Skill is a Jewel Socket
    var name = ""
    // this is all number of passive skills
    //Adds # Passive Skills
    var count = 0.0
    // how much sockets is in cluster jewel
    var socketCount = 0.0
    //Added Small Passive Skills also grant: +#% to Elemental Resistance
    var grants = ""
    //TODO Passive Skills in Radius also grant: Traps and Mines deal # to # added Physical Damage
    //TODO Notable Passive Skills in Radius are Transformed to
  }
  object DoT {
    object multiplier {
      var general = 0.0
      var cold = 0.0
      var fire = 0.0
      var chaos = 0.0
      var nonAilmentChaos = 0.0
      var physical = 0.0
      var bleeding = 0.0
    }
  }

  }
