# Evolving Shields

This file provides the details about the Evolving Shield feature.

Index:

- [Configuration](#configuration)
- [What is an evolving shield?](#what-is-an-evolving-shield)
- [Levelling up](#levelling-up)
- [Stages](#stages)

## Configuration

### Enable evolving shields

* Note - enabling evolving shields will disable shield enchantments in the enchantment bench, if you have that configured

`/uhc set enable.evolving.shields=true`

### Set experience -> damage threshold

Set the experience threshold before levelling up the shield requires doing player damage instead of gaining minecraft exp.

You can use this to ensure that higher stages can't be grinded with an XP farm.

This defaults to -1 (no limit for minecraft XP)

`/uhc set evolving.shields.exp.threshold=2000`

## What is an evolving shield?

On UHC start, each player will be given a unique shield to them. They cannot drop, destroy or lose the shield in any way.

You can [level up](#levelling-up) your shield and [unlock upgrades](#stages) from doing so.

If you die, your shield is destroyed. It cannot be picked up by opponents, or teammates.

If you are revived, you will be given a new evolving shield that has 0 XP.

## Levelling up

As references in the prior section, levelling up your shield can be done in one of a couple ways.

### Gaining minecraft XP

Gaining minecraft XP will level up the shield (up to and including the `evolving.shields.exp.threshold` config value).

Every `n` minecraft XP equals `n * 4` EXP for the shield.

What this means in real-world terms is (for example):

- Killing a chicken: 4-9 EXP
- Killing a creeper: 24-32 EXP
- Mining/smelting iron: 2.8 EXP per ingot

For more information, see https://minecraft.wiki/w/Experience

### Dealing damage to opponents

Dealing damage to opponents will level up the shield. 

This can be via melee or projectile.

* Note - TNT damage is not included

Every half a heart of post-armor calculated damage equals **100 EXP**

What this means in real-world terms is (for example):

- Hitting a player with full iron armor with an Iron Sword: 300 EXP (1.5 hearts of damage)
- Hitting a player with full iron armor with an Iron Axe: 500 EXP (2.5 hearts of damage) 
- Critting an unarmored player with a Golden Sword: 600 EXP (3 hearts of damage)
- Critting a player with full iron armor player with a Diamond Axe: 900 EXP (4.5 hearts of damage)

### Claiming a loot chest

The first player to open the loot chest after it has moved location is awarded some EXP.

Claiming a loot chest will give you a 200 EXP bonus.

### Reviving a teammate

Reviving a teammate will give you award you some EXP.

After your teammate has been revived, you are awarded a 500 EXP bonus.

### Killing opponents

Killing opponents will give you an extra bonus bit of EXP.

Killing an opponent will give a 250 EXP bonus.

* Note: You must deal the final blow to gain the EXP. If they die from fall damage, a potion effect, TNT, etc. You will not gain his bonus.

## Stages

As you level up, you will reach different stages of the evolving shield. Each stage will provide you with a choice of bonuses or upgrades to pick from. Once picked, there is no going back, so choose wisely.

See the stages below:

### Stage 1 (30 EXP)

- 32x Steak
- 4x Book
- 4x Apple
- 12x Iron Ingots

### Stage 2 (100 EXP)

- Add 'Knockback I' to your shield
- Add 'Thorns I' to your shield

### Stage 3 (400 EXP)

- Gain permanent Absorption (2 extra hearts)
- Regenerate 4 hearts immediately
- Gain a player head

### Stage 4 (700 EXP)

- 18x Arrow
- 12x Spectral Arrow
- 3x Tipped Arrow with Instant Damage II applied

### Stage 5 (1100 EXP)

- Add 'Swift Defense' to your shield. Chance of gaining swiftness after blocking an attack (1/16)
- Add 'Leap Guard' to your shield. Chance of gaining jump boost after blocking an attack (1/8)
- Add 'Counterforce' to your shield. Chance of gaining strength after blocking an attack (1/24)

### Stage 6 (1600 EXP)

- Add 'Sapping Guard' to your shield. Chance of giving weakness after blocking an attack (1/16)
- Add 'Snare Guard' to your shield. Chance of giving slowness after blocking an attack (1/8)

### Stage 7 (2100 EXP)

- Upgrade or add 'Knockback' to your shield.
- Upgrade or add 'Thorns' to your shield.

### Stage 8 (3000 EXP)

- Gain the _Nether Explorer_ kit
  - 10x Obsidian
  - 1x Golden Helmet
  - 1x Flint and Steel
- Gain the _Apothecary_ kit
  - 1x Brewing Stand
  - 3x Nether Wart
  - 1x Blaze Powder
  - 3x Water Bottle
- Gain the _Librarian_ kit
  - 1x Knockback II Enchanted Book
  - 1x Sharpness II Enchanted Book
  - 1x Protection II Enchanted Book
  - 4x Bookshelf
  - 32x EXP Bottle

### Stage 9 (3500 EXP)

- 1x Quickboom IV TNT
- 1x Blastwave IV TNT
- 1x Quickboom II & Blastwave II TNT

### Stage 10 (5000 EXP)

- Add 'Fire Elemental' to your shield. Blocking whilst sneaking will shoot a fire charge (60 second cooldown)
- Add 'Thunder Elemental' to your shield. Blocking whilst sneaking will strike a thunderbolt on the block you're looking at. (120 second cooldown)
- Add 'Air Elemental' to your shield. Blocking whilst sneaking will shoot a wind charge (30 second cooldown)
- Add 'Water Elemental' to your shield. Blocking whilst sneaking will shoot a snowball that creates a bucket of water (30 second cooldown)

### Stage 11 (7500 EXP)

- Become the **Reaper**
  - 2x Ender Pearl
  - 4x Tipped Arrow (Wither II applied for 8s)
  - 1x Hoe (Enchanted with 'Reaper Marked')
    - 'Reaper Marked' gives a chance (50%) of applying 'Wither I (00:04)' on damage.
- Become the **Phoenix**
  - 1x Elytra
  - 1x Iron Spear (Fire Aspect I)
  - Iron Boots (Feather Falling IV, Fire Protection IV)
  - 8x Firework Rocket