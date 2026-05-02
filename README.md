# Modern Damage Control

一个为 Minecraft 带来真实部位伤害、先进护甲机制和战术医疗系统的模组。

> ⚠️ 注意：本模组中的所有医疗物品仅为游戏设计，不构成现实世界的医疗建议。

---

## 前置依赖

- **Minecraft Forge** 1.20.1
- **TACZ (Timeless and Classics Zero)** – 必需！本模组的子弹精确命中部位判定完全依赖 TACZ。
- **Cloth Config API** – 必需，用于管理配置文件。

> 缺少 TACZ 或 Cloth Config 时模组将无法启动。

---

## 核心功能

### 两种伤害模式

- **软核 (SOFT)**：原版单一血量。死亡条件：原版血量归零，仅部位受击惩罚。
- **硬核 (HARDCORE)**：包括玩家与所有实体的7部位独立血量。死亡条件：头部/胸部血量归零 或 总血量归零，部位血量归零时溢出伤害+被摧毁时施加药水效果。

可在配置文件中切换。

### 部位伤害系统

- 7 个独立部位：头部、胸部、胃部、左臂、右臂、左腿、右腿。
- 子弹命中时根据 TACZ 的命中坐标计算部位。
- 每个部位可独立配置受伤时触发和部位摧毁时触发的药水效果（如出血、骨折、眩晕等）。

### 护甲与穿透机制

- 护甲按部位提供防护能力（1–100点），防护能力随耐久线性衰减（最低保留 50%）。
- 此外从attributes处可增加各个部位的天然护甲属性，天然护甲属性会与装备护甲一样产生防护能力。
- TACZ 枪支的 护甲穿透属性（armor_ignore）会参与计算（每1%可视为穿透1点防护能力），产生完全穿透、部分穿透、钝伤三种结果。
- 支持为任意物品（包括其他模组的装备）配置防护数据，包括护甲的防护部位与每个防护部位的具体防护能力。

### 战术医疗系统

- **伤口处理**：无菌纱布、CAT-7 止血带、以色列绷带（止血）。
- **骨折处理**：SAM 夹板（固定）、3M 石膏绷带（完全愈合）。
- **创伤修复**：Dr. Stitch 缝合线、皮肤缝合器（修复严重创伤）。
- **药物**：布洛芬 / 曲马多（止痛）、肌酸（力量+速度+抗疲劳）、咖啡因（耐力）、莫达非尼（专注）。
- **静脉输液**：生理盐水、林格液、人造血白蛋白等，提供持续生命恢复。
- **医疗包**：6个包含不同功能与属性急救包（止血、止痛、修复创伤）。

### 负面状态与抵抗

- **出血**：轻微出血 / 大出血 – 持续掉血。
- **骨折**：手臂（降低近战伤害与射击精度）/ 腿部（减速、无法疾跑）。
- **严重创伤**：持续掉血，随机触发剧痛。
- **眩晕**：大幅减速、恶心、禁止疾跑。
- **疼痛**：限制移动、黑暗效果。
- **抵抗效果**：止痛、凝血增强、眩晕抵抗、中毒抵抗、感染抵抗。

### 生物部位血量（硬核模式）

- 硬核模式下可为非玩家实体启用部位血量。
- 可配置生物部位受伤/摧毁时施加的效果（如僵尸断腿 → 缓慢）。

### 客户端 HUD（硬核模式）

- 显示玩家 7 个部位的健康状态（纹理随血量百分比变化）。
- 受伤时闪烁反馈。
- 位置、缩放可配置。

---

## 命令

| 命令 | 说明 |
|------|------|
| `/moderndamage parthealth [target]` | 查看自己、其他玩家或生物的部位血量（硬核模式） |
| `/moderndamage debug hitbox <target>` | 调试输出目标实体的碰撞箱与判定部位 |
| `/moderndamage generate entity_config` | 生成默认的实体配置文件（命中箱、天然护甲） |

---

## 配置

- 以下配置文件位于 `.minecraft/config/`：
    - `moderndamage.json5` – 主配置（伤害模型、部位比例、药水效果、HUD 等）
- 以下配置文件位于 `.minecraft/config/moderndamage/`：
    - `armor_properties.json` – 自定义护甲的防护能力
    - `entity_config.json` – 实体的命中箱范围与天然护甲

---

## 兼容性

- **EnhancedVisuals**（可选）：疼痛/眩晕时播放心跳、耳鸣等屏幕特效。
- **Spore**（可选）：感染抵抗效果可清除菌丝感染。
- **tacz_attributes**（可选）：肌酸效果可调整后坐力属性。
- **staminafortweakers**（可选）：咖啡因、肌酸等效果影响耐力系统。

> 缺少可选依赖不会导致崩溃，相关功能自动禁用。

---

## 安装

1. 安装 **Forge 1.20.1**。
2. 安装 **TACZ** 和 **Cloth Config API**。
3. 将本模组的 JAR 文件放入 `mods` 文件夹。
4. 启动游戏，配置文件会自动生成。

---

## 开发与开源

- 源码许可：**MIT**

---

**愿每一颗子弹都打在正确的地方。** 🎯



# Modern Damage Control

A mod that brings realistic body part damage, advanced armor mechanics, and a tactical medical system to Minecraft.

> ⚠️ **Note**: All medical items in this mod are for game design purposes only and do not constitute real-world medical advice.

---

## Dependencies

- **Minecraft Forge** 1.20.1
- **TACZ (Timeless and Classics Zero)** – Required! Bullet hit location detection depends entirely on TACZ.
- **Cloth Config API** – Required for configuration management.

> The mod will not start without TACZ and Cloth Config API.

---

## Core Features

### Two Damage Models

- **SOFT mode**: Vanilla single health pool. Death condition: vanilla health reaches zero. Only status effects on body part hits.
- **HARDCORE mode**: 7 independent body parts for players and all entities. Death condition: head/chest health reaches zero OR total body part health reaches zero. Overflow damage and destruction effects apply when a part is destroyed.

Switchable via config file.

### Body Part Damage System

- **7 body parts**: Head, Chest, Stomach, Left Arm, Right Arm, Left Leg, Right Leg.
- Bullet impact location is precisely calculated using TACZ's hit detection.
- Each part can be configured with **on-hit** and **on-destroy** potion effects (e.g., bleeding, fractures, dizziness).

### Armor & Penetration Mechanics

- Armor provides **protection points** (1–100) per body part, which degrade linearly with durability (minimum 50% retained).
- **Natural armor** can be added via attributes, which functions like worn armor.
- TACZ's **armor_ignore** stat (1% = 1 penetration point) is used to determine **complete penetration**, **partial penetration**, or **blunt damage**.
- Supports defining protection data for any item (including from other mods): protected parts and protection points per part.

### Tactical Medical System

- **Wound treatment**: Sterile Gauze, CAT-7 Tourniquet, Israeli Bandage (stop bleeding).
- **Fracture treatment**: SAM Splint (stabilize), 3M Cast Tape (full healing).
- **Trauma repair**: Dr. Stitch Suture, Skin Stapler (repair severe trauma).
- **Medications**: Ibuprofen / Tramadol (pain relief), Creatine (strength+speed+fatigue resistance), Caffeine (stamina), Modafinil (focus).
- **IV fluids**: Saline, Ringer's Solution, Albumin, etc. – provide sustained health regeneration.
- **Medical kits**: 6 different multi‑purpose kits (stop bleeding, pain relief, trauma repair).

### Negative Status Effects & Resistances

- **Bleeding**: Minor / Major – periodic damage.
- **Fracture**: Arm (reduced melee damage and accuracy) / Leg (slowness, cannot sprint).
- **Severe trauma**: Periodic damage, random pain spikes.
- **Dizziness**: Severe slowness, nausea, cannot sprint.
- **Pain**: Movement restriction, darkness effect.
- **Resistances**: Pain suppression, coagulation boost, dizziness resistance, poison resistance, infection resistance.

### Creature Body Parts (Hardcore mode)

- Hardcore mode can enable body parts for non‑player entities.
- Configurable on‑hit and on‑destroy effects for mobs (e.g., zombie broken leg → slowness).

### Client HUD (Hardcore mode)

- Displays health status of the player's 7 body parts (texture changes with health percentage).
- Flash effect on injury.
- Position and scale configurable.

---

## Commands

| Command | Description |
|---------|-------------|
| `/moderndamage parthealth [target]` | View body part health of yourself, another player, or a mob (Hardcore mode) |
| `/moderndamage debug hitbox <target>` | Debug output of target entity's hitbox and detected body part |
| `/moderndamage generate entity_config` | Generate default entity configuration file (hitbox ranges, natural armor) |

---

## Configuration

- File located in `.minecraft/config/`:
    - `moderndamage.json5` – Main config (damage model, part ratios, potion effects, HUD, etc.)
- Files located in `.minecraft/config/moderndamage/`:
    - `armor_properties.json` – Custom armor protection data
    - `entity_config.json` – Entity hitbox ranges and natural armor

---

## Compatibility

- **EnhancedVisuals** (optional): Heartbeat, ringing, blur effects for pain/dizziness.
- **Spore** (optional): Infection resistance can remove mycelium infection.
- **tacz_attributes** (optional): Creatine effect can adjust recoil attributes.
- **staminafortweakers** (optional): Caffeine and creatine affect stamina system.

> Missing optional dependencies will not crash the mod; related features are automatically disabled.

---

## Installation

1. Install **Forge 1.20.1**.
2. Install **TACZ** and **Cloth Config API**.
3. Put the mod's JAR file into the `mods` folder.
4. Launch the game – config files will be generated automatically.

---

## Development & Open Source

- License: **MIT**

---

**May every bullet hit the right spot.** 🎯