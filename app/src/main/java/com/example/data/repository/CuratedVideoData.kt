package com.example.data.repository

import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem

object CuratedVideoData {

    val INTRO_WELCOME_VIDEO = VideoItem(
        id = "h0EGCnBjTVk",
        title = "I Outsmarted Pro Car Thieves With Science!",
        channelTitle = "Mark Rober",
        description = "Former NASA engineer Mark Rober creates the ultimate high-tech bait car using science and engineering.",
        category = VideoCategory.SCIENCE,
        ageGroup = AgeGroup.TEEN,
        durationText = "20:30",
        tags = listOf("mark rober", "science", "engineering", "cool"),
        viewCountText = "48M views"
    )

    val CURATED_VIDEOS: List<VideoItem> = listOf(
        // ==========================================
        // 🍳 COOKING & FOOD EXPERIMENTS (VERIFIED)
        // ==========================================
        VideoItem(
            id = "wv7_S_B1XbQ",
            title = "MINECRAFT Grass Block Cake Tutorial!",
            channelTitle = "Rosanna Pansino",
            description = "Baking a pixel-perfect square chocolate fudge cake layered with green matcha grass icing.",
            category = VideoCategory.COOKING,
            ageGroup = AgeGroup.EARLY,
            durationText = "15:30",
            tags = listOf("cooking", "minecraft", "cake", "baking", "nerdy nummies"),
            viewCountText = "34M views"
        ),
        VideoItem(
            id = "Q6vVUb2jicU",
            title = "How To Skin A Watermelon (Party Trick!)",
            channelTitle = "Mark Rober",
            description = "Former NASA engineer Mark Rober teaches the ultimate clean-skinned watermelon summer food trick.",
            category = VideoCategory.COOKING,
            ageGroup = AgeGroup.EARLY,
            durationText = "6:22",
            tags = listOf("cooking", "watermelon", "science", "party", "food"),
            viewCountText = "130M views"
        ),
        VideoItem(
            id = "0bfK90wyG3I",
            title = "World's Largest Jell-O Pool Experiment",
            channelTitle = "Mark Rober",
            description = "Is it possible to swim in a giant swimming pool filled entirely with 25 tons of Jell-O?",
            category = VideoCategory.COOKING,
            ageGroup = AgeGroup.EARLY,
            durationText = "14:25",
            tags = listOf("mark rober", "science", "jello", "experiments", "fun"),
            viewCountText = "95M views"
        ),
        VideoItem(
            id = "ugPnAnu5q8g",
            title = "Testing If Sharks Can Really Smell Blood!",
            channelTitle = "Mark Rober",
            description = "Marine biology and scientific ocean experiments testing shark olfactory senses safely.",
            category = VideoCategory.COOKING,
            ageGroup = AgeGroup.EARLY,
            durationText = "16:40",
            tags = listOf("science", "animals", "ocean", "nature", "sharks"),
            viewCountText = "110M views"
        ),
        VideoItem(
            id = "mOS4u6qZ_pI",
            title = "World's Largest Nerf Gun Challenge",
            channelTitle = "Mark Rober",
            description = "Engineering a massive 4-foot pressurized dart cannon firing custom foam darts at 40mph!",
            category = VideoCategory.COOKING,
            ageGroup = AgeGroup.TEEN,
            durationText = "10:15",
            tags = listOf("science", "engineering", "nerf", "fun"),
            viewCountText = "75M views"
        ),

        // ==========================================
        // 🎵 EXPANDED MUSIC SECTION (AMR DIAB, ARCTIC MONKEYS, KATSEYE, POP & ROCK HITS)
        // ==========================================
        // --- AMR DIAB (ARABIC POP & LEGENDARY HITS) ---
        VideoItem(
            id = "0goS0jTiIkA",
            title = "Amr Diab - Nour El Ein (Official Music Video)",
            channelTitle = "Amr Diab",
            description = "Habibi ya nour el ein ya sakin khayali — Amr Diab's iconic Latin-Arabic masterpiece that conquered world charts.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "5:08",
            tags = listOf("amr diab", "nour el ein", "arabic", "music", "latin pop", "classic", "hits"),
            viewCountText = "210M views"
        ),
        VideoItem(
            id = "m_9la60yF68",
            title = "Amr Diab - Tamally Maak (Official Music Video)",
            channelTitle = "Amr Diab",
            description = "Tamally maak, we law hata baeed any fe alby hawak — the legendary romantic ballad loved by millions worldwide.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:32",
            tags = listOf("amr diab", "tamally maak", "arabic", "romantic", "music", "hits", "ballad"),
            viewCountText = "340M views"
        ),
        VideoItem(
            id = "3O1_3zBUbGs",
            title = "Amr Diab - Inta El Haz (Official Lyric Video)",
            channelTitle = "Amr Diab",
            description = "Inta el haz ya haz b nafso — the energetic chart-topping modern summer anthem by Amr Diab.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "3:40",
            tags = listOf("amr diab", "inta el haz", "arabic", "pop", "summer", "music"),
            viewCountText = "140M views"
        ),
        VideoItem(
            id = "kYw2-qU58yE",
            title = "Amr Diab - Wayah (Official Music Video)",
            channelTitle = "Amr Diab",
            description = "Ana wayah el hayah hatkoon ahla keteer — the vibrant upbeat Mediterranean pop hit from the album Wayah.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:50",
            tags = listOf("amr diab", "wayah", "arabic", "pop", "dance", "music"),
            viewCountText = "95M views"
        ),
        VideoItem(
            id = "b1Y8Hn7S1g8",
            title = "Amr Diab - Amarain (Official Music Video)",
            channelTitle = "Amr Diab",
            description = "Amarain dol wala ainain — the timeless flamenco-infused pop celebration featuring legendary rhythm and acoustic guitars.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:24",
            tags = listOf("amr diab", "amarain", "arabic", "flamenco", "music", "hits"),
            viewCountText = "85M views"
        ),

        // --- ARCTIC MONKEYS (INDIE ROCK & GLOBAL ANTHEMS) ---
        VideoItem(
            id = "bpOSxM0rNPM",
            title = "Arctic Monkeys - Do I Wanna Know? (Official Music Video)",
            channelTitle = "Arctic Monkeys",
            description = "Crawling back to you — the iconic Grammy-nominated rock track with hypnotic soundwave visuals and heavy blues guitar riffs.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:25",
            tags = listOf("arctic monkeys", "do i wanna know", "rock", "indie rock", "alex turner", "am", "music"),
            viewCountText = "1.6B views"
        ),
        VideoItem(
            id = "VQH8ZTgna3Q",
            title = "Arctic Monkeys - R U Mine? (Official Music Video)",
            channelTitle = "Arctic Monkeys",
            description = "Are you mine tomorrow? Or just mine tonight? Heavy distorted bass, punchy drums, and relentless garage-rock speed.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "3:43",
            tags = listOf("arctic monkeys", "r u mine", "rock", "indie rock", "alex turner", "music"),
            viewCountText = "450M views"
        ),
        VideoItem(
            id = "6366dxFf-Os",
            title = "Arctic Monkeys - Why'd You Only Call Me When You're High? (Official Video)",
            channelTitle = "Arctic Monkeys",
            description = "The moody nocturnal hip-hop influenced groove from the seminal album AM, directed by Nabil.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:47",
            tags = listOf("arctic monkeys", "high", "indie rock", "rock", "alex turner", "music"),
            viewCountText = "310M views"
        ),
        VideoItem(
            id = "qU9mHegkTc4",
            title = "Arctic Monkeys - 505 (Official Audio)",
            channelTitle = "Arctic Monkeys",
            description = "I'm going back to 505, if it's a 7 hour flight or a 45 minute drive — the beloved crescendo rock anthem.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:13",
            tags = listOf("arctic monkeys", "505", "favourite worst nightmare", "rock", "indie", "alex turner"),
            viewCountText = "380M views"
        ),
        VideoItem(
            id = "ma9I9VBKPiw",
            title = "Arctic Monkeys - Fluorescent Adolescent (Official Music Video)",
            channelTitle = "Arctic Monkeys",
            description = "You used to get it in your fishnets, now you only get it in your night dress — classic British indie rock nostalgia.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "3:03",
            tags = listOf("arctic monkeys", "fluorescent adolescent", "indie rock", "britpop", "music"),
            viewCountText = "220M views"
        ),

        VideoItem(
            id = "60ItHLz5WEA",
            title = "Alan Walker - Faded (Official Music Video)",
            channelTitle = "Alan Walker",
            description = "The record-breaking global electronic pop anthem Faded by Alan Walker with iconic melodies and atmosphere.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "3:32",
            tags = listOf("alan walker", "faded", "electronic", "pop", "music", "hits"),
            viewCountText = "3.6B views"
        ),
        VideoItem(
            id = "n8X9_FX26mQ",
            title = "TheFatRat - Unity (Official Track)",
            channelTitle = "TheFatRat",
            description = "High energy melodic gaming and pop hit Unity with electrifying synths and uplifting rhythm.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:09",
            tags = listOf("thefatrat", "unity", "gaming", "music", "electronic", "pop"),
            viewCountText = "260M views"
        ),
        VideoItem(
            id = "09R8_2nJtjg",
            title = "Maroon 5 - Sugar (Official Music Video)",
            channelTitle = "Maroon 5",
            description = "Maroon 5 crashes real weddings all around Los Angeles, delivering unforgettable live surprise performances of Sugar!",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "5:01",
            tags = listOf("maroon 5", "sugar", "pop", "music", "wedding", "adam levine", "hits"),
            viewCountText = "4.1B views"
        ),
        VideoItem(
            id = "SlPhetYn6gM",
            title = "Maroon 5 - Memories (Official Video)",
            channelTitle = "Maroon 5",
            description = "Here's to the ones that we got, cheers to the wish you were here — the emotional acoustic hit single by Maroon 5.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "3:15",
            tags = listOf("maroon 5", "memories", "pop", "acoustic", "music", "adam levine"),
            viewCountText = "1.1B views"
        ),
        VideoItem(
            id = "KtC-gLSCyB0",
            title = "Maroon 5 - Moves Like Jagger ft. Christina Aguilera",
            channelTitle = "Maroon 5",
            description = "The iconic multi-platinum dance-pop anthem celebrating legendary rhythm and moves.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:39",
            tags = listOf("maroon 5", "moves like jagger", "pop", "dance", "music", "christina aguilera"),
            viewCountText = "820M views"
        ),
        VideoItem(
            id = "KRaWnd3LJfs",
            title = "Maroon 5 - Payphone (Official Music Video)",
            channelTitle = "Maroon 5",
            description = "Adam Levine stars in an action-packed cinematic music video for the worldwide smash hit Payphone.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "4:39",
            tags = listOf("maroon 5", "payphone", "pop", "music", "adam levine"),
            viewCountText = "930M views"
        ),
        VideoItem(
            id = "eVli-tstM5E",
            title = "Sabrina Carpenter - Espresso (Official Music Video)",
            channelTitle = "Sabrina Carpenter",
            description = "The record-breaking summer pop anthem with infectious retro beach energy and irresistible melody.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "3:18",
            tags = listOf("sabrina carpenter", "espresso", "pop", "music", "summer", "dance"),
            viewCountText = "210M views"
        ),
        VideoItem(
            id = "gdZLi9oWNZg",
            title = "BTS - Dynamite (Official Music Video)",
            channelTitle = "HYBE LABELS",
            description = "Cos ah ah I’m in the stars tonight, so watch me bring the fire and set the night alight — the Grammy-nominated global phenomenon.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "3:43",
            tags = listOf("bts", "dynamite", "kpop", "pop", "dance", "hybe", "music"),
            viewCountText = "1.9B views"
        ),
        VideoItem(
            id = "Vk5-c_v4gMU",
            title = "ILLIT - Magnetic (Official Music Video)",
            channelTitle = "HYBE LABELS",
            description = "The ultra-catchy viral hit by ILLIT that took over charts worldwide with its sparkling pluggnb and house bounce.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "3:08",
            tags = listOf("illit", "magnetic", "kpop", "pop", "hybe", "music", "dance"),
            viewCountText = "280M views"
        ),
        VideoItem(
            id = "hT_nvWreIhg",
            title = "OneRepublic - Counting Stars (Official Music Video)",
            channelTitle = "OneRepublic",
            description = "Lately I've been losing sleep, dreaming about the things that we could be — OneRepublic's diamond-certified classic.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "4:43",
            tags = listOf("onerepublic", "counting stars", "pop rock", "music", "ryan tedder"),
            viewCountText = "4.2B views"
        ),
        VideoItem(
            id = "PEM0Vs8jf1w",
            title = "JVKE - golden hour (Official Music Video)",
            channelTitle = "JVKE",
            description = "It was just two lovers sittin' in the car, listenin' to Blonde, fallin' for each other — JVKE's cinematic orchestral pop masterpiece.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "3:51",
            tags = listOf("jvke", "golden hour", "piano", "pop", "music", "cinematic"),
            viewCountText = "260M views"
        ),
        VideoItem(
            id = "7wtfhZwyrcc",
            title = "Imagine Dragons - Believer (Official Music Video)",
            channelTitle = "ImagineDragons",
            description = "First things first, I'ma say all the words inside my head — the explosive stadium-rock hit by Imagine Dragons.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.TEEN,
            durationText = "3:36",
            tags = listOf("imagine dragons", "believer", "rock", "pop", "music", "anthem"),
            viewCountText = "2.7B views"
        ),
        VideoItem(
            id = "nfWlot6h_JM",
            title = "Taylor Swift - Shake It Off (Official Music Video)",
            channelTitle = "TaylorSwift",
            description = "'Cause the players gonna play, play, play, play, play — the joyous diamond pop anthem by Taylor Swift.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "4:01",
            tags = listOf("taylor swift", "shake it off", "pop", "dance", "music", "happy"),
            viewCountText = "3.4B views"
        ),
        VideoItem(
            id = "VPRjCeoBqrI",
            title = "Coldplay - A Sky Full of Stars (Official Music Video)",
            channelTitle = "Coldplay",
            description = "Chris Martin and Coldplay take to the streets of Sydney with one-man-band instruments in this euphoric hit.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "4:13",
            tags = listOf("coldplay", "a sky full of stars", "pop", "uplifting", "music"),
            viewCountText = "980M views"
        ),
        VideoItem(
            id = "bvWRMAU6V-c",
            title = "We Don't Talk About Bruno (From \"Encanto\")",
            channelTitle = "DisneyMusicVEVO",
            description = "The smash hit song from Disney's Encanto featuring the magical Madrigal family.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "3:42",
            tags = listOf("music", "disney", "encanto", "sing along", "animation"),
            viewCountText = "540M views"
        ),
        VideoItem(
            id = "L0MK7qz13bU",
            title = "Let It Go - Sing-Along Edition (Disney's Frozen)",
            channelTitle = "DisneyMusicVEVO",
            description = "Sing along with Queen Elsa to the Oscar-winning song Let It Go from Frozen.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "3:44",
            tags = listOf("music", "frozen", "let it go", "disney", "sing along"),
            viewCountText = "780M views"
        ),
        VideoItem(
            id = "cPAbx5kgCJo",
            title = "How Far I'll Go - Moana (Official Sing-Along)",
            channelTitle = "DisneyMusicVEVO",
            description = "Auli'i Cravalho performs How Far I'll Go from Disney's epic animated ocean adventure Moana.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "2:36",
            tags = listOf("music", "moana", "disney", "sing along", "inspiring"),
            viewCountText = "390M views"
        ),
        VideoItem(
            id = "ru0K8uYEZWw",
            title = "CAN'T STOP THE FEELING! (From DreamWorks Trolls)",
            channelTitle = "Justin Timberlake",
            description = "The ultimate upbeat dance party anthem that will get everyone smiling and dancing!",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "4:45",
            tags = listOf("music", "dance", "trolls", "happy", "party"),
            viewCountText = "1.6B views"
        ),
        VideoItem(
            id = "QgaTQ5-XfMM",
            title = "A Thousand Years - Cello & Piano Instrumental",
            channelTitle = "The Piano Guys",
            description = "A breathtakingly beautiful instrumental cello and piano performance in nature.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "4:35",
            tags = listOf("music", "piano", "cello", "classical", "relaxing"),
            viewCountText = "240M views"
        ),
        VideoItem(
            id = "WPni755-Krg",
            title = "Relaxing Studio Ghibli Piano Melody Collection",
            channelTitle = "Calm Melodies",
            description = "Soft, soothing piano music inspired by animated classics, perfect for reading and creativity.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "1:02:15",
            tags = listOf("music", "piano", "ghibli", "chill", "study"),
            viewCountText = "35M views"
        ),
        VideoItem(
            id = "fKopy74weus",
            title = "Calm Relaxing Piano & Nature Beats",
            channelTitle = "Soothing Sounds",
            description = "Peaceful relaxing piano melodies with soft ambient rainfall for focus and calm.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "3:00:00",
            tags = listOf("music", "piano", "relaxing", "chill", "study"),
            viewCountText = "45M views"
        ),
        VideoItem(
            id = "jfKfPfyJRdk",
            title = "lofi hip hop radio - beats to relax/study to",
            channelTitle = "Lofi Girl",
            description = "The iconic peaceful 24/7 stream of calming instrumental beats and chill vibes.",
            category = VideoCategory.MUSIC,
            ageGroup = AgeGroup.EARLY,
            durationText = "Live",
            tags = listOf("lofi", "chill", "study", "relax", "music", "beats"),
            viewCountText = "Live"
        ),

        // ==========================================
        // 🔬 SCIENCE & ENGINEERING (MARK ROBER & MORE)
        // ==========================================
        VideoItem(
            id = "h0EGCnBjTVk",
            title = "I Outsmarted Pro Car Thieves With Science!",
            channelTitle = "Mark Rober",
            description = "Former NASA engineer Mark Rober creates the ultimate high-tech bait car to catch car break-in rings.",
            category = VideoCategory.SCIENCE,
            ageGroup = AgeGroup.TEEN,
            durationText = "20:30",
            tags = listOf("mark rober", "science", "engineering", "prank"),
            viewCountText = "48M views"
        ),
        VideoItem(
            id = "3c584TGG7jQ",
            title = "Glitter Bomb 5.0 - The World's Most Advanced Trap",
            channelTitle = "Mark Rober",
            description = "Engineering custom circuit boards, micro air cannons, and GPS tracking to outwit porch thieves.",
            category = VideoCategory.SCIENCE,
            ageGroup = AgeGroup.TEEN,
            durationText = "28:12",
            tags = listOf("mark rober", "engineering", "glitter bomb", "science"),
            viewCountText = "75M views"
        ),
        VideoItem(
            id = "hFZFjoX2cGg",
            title = "Building The Ultimate Backyard Squirrel Obstacle Course",
            channelTitle = "Mark Rober",
            description = "Can crafty neighborhood squirrels conquer an elaborate ninja obstacle course? Physics in action!",
            category = VideoCategory.SCIENCE,
            ageGroup = AgeGroup.EARLY,
            durationText = "21:40",
            tags = listOf("mark rober", "animals", "squirrels", "obstacle course", "fun"),
            viewCountText = "120M views"
        ),
        VideoItem(
            id = "9IiYOTzJ2uw",
            title = "I Built a Roller Coaster In My Lab!",
            channelTitle = "Mark Rober",
            description = "Engineering a full looping roller coaster inside a workshop testing centrifugal forces.",
            category = VideoCategory.SCIENCE,
            ageGroup = AgeGroup.TEEN,
            durationText = "18:15",
            tags = listOf("mark rober", "science", "roller coaster", "experiments"),
            viewCountText = "38M views"
        ),
        VideoItem(
            id = "0bfK90wyG3I",
            title = "World's Largest Jell-O Pool Experiment",
            channelTitle = "Mark Rober",
            description = "Is it possible to swim in a giant swimming pool filled entirely with 25 tons of Jell-O?",
            category = VideoCategory.SCIENCE,
            ageGroup = AgeGroup.EARLY,
            durationText = "14:25",
            tags = listOf("mark rober", "science", "experiments", "fun"),
            viewCountText = "95M views"
        ),

        // ==========================================
        // 🏆 SPORTS & STUNTS (DUDE PERFECT & EXTREME)
        // ==========================================
        VideoItem(
            id = "VJwoSfTOhyM",
            title = "Water Bottle Flip 2 | Dude Perfect",
            channelTitle = "Dude Perfect",
            description = "The craziest bottle flip landing trick shots ever attempted across rooftops and giant towers.",
            category = VideoCategory.SPORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "7:18",
            tags = listOf("dude perfect", "bottle flip", "trick shots"),
            viewCountText = "150M views"
        ),
        VideoItem(
            id = "VRJmcxCrAOA",
            title = "Real Life Trick Shots 2 | Dude Perfect",
            channelTitle = "Dude Perfect",
            description = "Dude Perfect takes insane trick shots into everyday real-life situations! Unbelievable spins and baskets.",
            category = VideoCategory.SPORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "8:35",
            tags = listOf("dude perfect", "trick shots", "sports", "comedy"),
            viewCountText = "115M views"
        ),
        VideoItem(
            id = "m3g6wP9C-uA",
            title = "Ping Pong Trick Shots 4 | Dude Perfect",
            channelTitle = "Dude Perfect",
            description = "Pots, pans, trampolines, and ceiling fans! Unbelievable precision ping pong trick shots.",
            category = VideoCategory.SPORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "7:52",
            tags = listOf("dude perfect", "ping pong", "trick shots", "stunts"),
            viewCountText = "180M views"
        ),

        // ==========================================
        // 🎮 GAMING & MINECRAFT CHALLENGES
        // ==========================================
        VideoItem(
            id = "kX3nB4PpJko",
            title = "${'$'}1 vs ${'$'}1,000,000 Hotel Room!",
            channelTitle = "MrBeast",
            description = "Exploring the most extreme, mind-blowing hotel rooms on Earth from budget to pure luxury.",
            category = VideoCategory.GAMING,
            ageGroup = AgeGroup.TWEEN,
            durationText = "24:10",
            tags = listOf("mrbeast", "challenge", "travel", "luxury", "fun"),
            viewCountText = "320M views"
        ),
        VideoItem(
            id = "frX3oGmVfpw",
            title = "World's Tallest LEGO Tower Challenge!",
            channelTitle = "MrBeast",
            description = "Constructing a record-breaking massive Lego skyscraper tower with thousands of bricks.",
            category = VideoCategory.GAMING,
            ageGroup = AgeGroup.TWEEN,
            durationText = "16:20",
            tags = listOf("lego", "building", "challenge", "creative", "toys"),
            viewCountText = "140M views"
        ),

        // ==========================================
        // ✂️ TRICK SHOTS & CREATIVITY
        // ==========================================
        VideoItem(
            id = "7i_R7HwGgO4",
            title = "Airplane Trick Shots - Dude Perfect",
            channelTitle = "Dude Perfect",
            description = "Tossing basketballs and trick shots out of airplanes, helicopters, and blimps!",
            category = VideoCategory.ART,
            ageGroup = AgeGroup.EARLY,
            durationText = "11:20",
            tags = listOf("dude perfect", "trick shots", "stunts", "extreme", "fun"),
            viewCountText = "98M views"
        ),
        VideoItem(
            id = "P7X8_6wFj68",
            title = "Ultimate Nerf Blasters Battle!",
            channelTitle = "Dude Perfect",
            description = "Dude Perfect builds the ultimate fortress and battles it out in high-stakes Nerf action.",
            category = VideoCategory.ART,
            ageGroup = AgeGroup.EARLY,
            durationText = "10:15",
            tags = listOf("dude perfect", "nerf", "battle", "game", "challenge"),
            viewCountText = "120M views"
        ),

        // ==========================================
        // 🌿 NATURE & WILDLIFE (SCIENCE ANIMATION)
        // ==========================================
        VideoItem(
            id = "h6fcK_fRYaI",
            title = "The Egg - A Short Story",
            channelTitle = "Kurzgesagt - In a Nutshell",
            description = "A philosophical, beautifully animated tale about humanity, connection, and the universe.",
            category = VideoCategory.NATURE,
            ageGroup = AgeGroup.TEEN,
            durationText = "8:00",
            tags = listOf("kurzgesagt", "animation", "philosophy", "story", "nature"),
            viewCountText = "38M views"
        ),
        VideoItem(
            id = "PaErPyEnDvk",
            title = "Deep Ocean Mysteries: The Midnight Zone",
            channelTitle = "Kurzgesagt - In a Nutshell",
            description = "Travel miles beneath the ocean surface to discover glowing bioluminescent deep sea creatures.",
            category = VideoCategory.NATURE,
            ageGroup = AgeGroup.TWEEN,
            durationText = "11:20",
            tags = listOf("nature", "ocean", "science", "creatures", "animation"),
            viewCountText = "31M views"
        ),

        // ==========================================
        // 🌟 CREATORS & FUN CHALLENGES
        // ==========================================
        VideoItem(
            id = "0e3GPea1Tyg",
            title = "${'$'}456,000 Real Life Game Challenge!",
            channelTitle = "MrBeast",
            description = "Recreating iconic stadium party games in real life with 456 players competing for the prize!",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "25:41",
            tags = listOf("creators", "challenge", "games", "trending", "fun"),
            viewCountText = "590M views"
        ),
        VideoItem(
            id = "zxYjTTXc-J8",
            title = "Last To Leave Giant Red Circle Wins ${'$'}500,000",
            channelTitle = "MrBeast",
            description = "100 people step inside a red circle, the last one remaining walks away with a giant prize!",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "16:28",
            tags = listOf("creators", "circle", "challenge", "money"),
            viewCountText = "330M views"
        ),

        // ==========================================
        // 💡 LEARNING & CODE
        // ==========================================
        VideoItem(
            id = "9IiYOTzJ2uw",
            title = "Building a Roller Coaster In My Lab!",
            channelTitle = "Mark Rober",
            description = "Physics and structural engineering in action: building a working indoor roller coaster track.",
            category = VideoCategory.EDUCATION,
            ageGroup = AgeGroup.TEEN,
            durationText = "18:45",
            tags = listOf("physics", "engineering", "mark rober", "science", "roller coaster"),
            viewCountText = "68M views"
        ),

        // ==========================================
        // 🌟 TOP CREATORS, TECHNOBLADE & GAMING LEGENDS
        // ==========================================
        // --- TECHNOBLADE (MINECRAFT LEGEND) ---
        VideoItem(
            id = "09CeBwGbCeg",
            title = "Skyblock: The Great Potato War 3 (FINALE)",
            channelTitle = "Technoblade",
            description = "The epic conclusion to the legendary 9-month Hypixel Skyblock potato farming rivalry with Squid Kid. Masterclass in strategy, dedication, and humor.",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "21:30",
            tags = listOf("technoblade", "minecraft", "potato war", "hypixel", "skyblock", "gaming", "legends"),
            viewCountText = "19M views"
        ),
        VideoItem(
            id = "DPMluEVUqS0",
            title = "so long nerds",
            channelTitle = "Technoblade",
            description = "Technoblade's final message to his community, read by his father. Thank you for supporting my content over the years. Technoblade never dies.",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "6:30",
            tags = listOf("technoblade", "so long nerds", "minecraft", "tribute", "legend", "gaming"),
            viewCountText = "130M views"
        ),
        VideoItem(
            id = "bJtG_p3ZqD8",
            title = "Beating Minecraft Hardcore Mode with a Steering Wheel",
            channelTitle = "Technoblade",
            description = "Can Technoblade defeat the Ender Dragon and complete Minecraft Hardcore mode using only a USB steering wheel and pedals?",
            category = VideoCategory.GAMING,
            ageGroup = AgeGroup.TEEN,
            durationText = "18:45",
            tags = listOf("technoblade", "minecraft", "steering wheel", "hardcore", "challenge", "gaming"),
            viewCountText = "24M views"
        ),
        VideoItem(
            id = "k_P_f1JgK9s",
            title = "I Almost Became The Mayor Of Skyblock",
            channelTitle = "Technoblade",
            description = "Technoblade stages a political campaign on Hypixel Skyblock with hilarious propaganda, speeches, and an unstoppable pig revolution.",
            category = VideoCategory.GAMING,
            ageGroup = AgeGroup.TEEN,
            durationText = "19:12",
            tags = listOf("technoblade", "skyblock", "hypixel", "mayor", "minecraft", "gaming"),
            viewCountText = "15M views"
        ),

        // --- MRBEAST (RECORD BREAKING CREATOR) ---
        VideoItem(
            id = "08lX_g4zWp8",
            title = "${'$'}456,000 Squid Game In Real Life!",
            channelTitle = "MrBeast",
            description = "Recreating every single game from Squid Game in real life with 456 contestants competing for a $456,000 cash prize.",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "25:41",
            tags = listOf("mrbeast", "squid game", "challenge", "creators", "viral", "fun"),
            viewCountText = "640M views"
        ),
        VideoItem(
            id = "glG6o_D7X9g",
            title = "I Spent 50 Hours In Solitary Confinement",
            channelTitle = "MrBeast",
            description = "MrBeast tests mental fortitude and endurance by spending 50 continuous hours completely isolated in a pure white room.",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "15:20",
            tags = listOf("mrbeast", "challenge", "creators", "solitary", "survival"),
            viewCountText = "310M views"
        ),

        // --- VERITASIUM & KURZGESAGT (TOP SCIENCE CREATORS) ---
        VideoItem(
            id = "094y1Z2wpJg",
            title = "The Simplest Math Problem No One Can Solve",
            channelTitle = "Veritasium",
            description = "The 3x + 1 problem (Collatz Conjecture) is so simple children understand it, but no mathematician on Earth has solved it.",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "22:15",
            tags = listOf("veritasium", "science", "math", "creators", "education", "collatz"),
            viewCountText = "42M views"
        ),
        VideoItem(
            id = "LE2v3sUzTH4",
            title = "The Last Human on Earth",
            channelTitle = "Kurzgesagt – In a Nutshell",
            description = "A thoughtful, visually stunning philosophical animated exploration of humanity's deep future and survival among the stars.",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "11:32",
            tags = listOf("kurzgesagt", "animation", "science", "space", "philosophy", "creators"),
            viewCountText = "26M views"
        ),

        // ==========================================
        // ⚡ AUTOPLAYING YOUTUBE SHORTS (VERTICAL 360x640 FEED)
        // ==========================================
        VideoItem(
            id = "Q6vVuj3iggE",
            title = "How To Skin A Watermelon (Party Trick!) ⚡",
            channelTitle = "Mark Rober",
            description = "Former NASA engineer Mark Rober teaches the ultimate clean-skinned watermelon summer food trick.",
            category = VideoCategory.SHORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "0:48",
            isShort = true,
            tags = listOf("shorts", "mark rober", "science", "food", "trick"),
            viewCountText = "130M views"
        ),
        VideoItem(
            id = "DPZzrlFCD_I",
            title = "World's Largest Jell-O Pool Experiment ⚡",
            channelTitle = "Mark Rober",
            description = "Is it possible to swim in a giant swimming pool filled entirely with 25 tons of Jell-O?",
            category = VideoCategory.SHORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "0:50",
            isShort = true,
            tags = listOf("shorts", "mark rober", "science", "experiments", "fun"),
            viewCountText = "95M views"
        ),
        VideoItem(
            id = "pVRefGmQDJM",
            title = "NASA Cosmic Deep Field Webb Imagery ⚡",
            channelTitle = "NASA",
            description = "Stunning ultra high definition deep cosmic imagery from the James Webb Space Telescope.",
            category = VideoCategory.SHORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "0:42",
            isShort = true,
            tags = listOf("shorts", "nasa", "space", "astronomy", "telescope"),
            viewCountText = "51M views"
        ),
        VideoItem(
            id = "ugRc5jx80yg",
            title = "Testing If Sharks Can Really Smell Blood! ⚡",
            channelTitle = "Mark Rober",
            description = "Marine biology and scientific ocean experiments testing shark olfactory senses safely.",
            category = VideoCategory.SHORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "0:55",
            isShort = true,
            tags = listOf("shorts", "mark rober", "ocean", "science", "sharks"),
            viewCountText = "110M views"
        ),
        VideoItem(
            id = "57MKxz4pJKE",
            title = "World's Largest High-Powered Nerf Cannon ⚡",
            channelTitle = "Mark Rober",
            description = "Engineering a massive pressurized dart cannon firing custom foam darts at high speed!",
            category = VideoCategory.SHORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "0:45",
            isShort = true,
            tags = listOf("shorts", "mark rober", "engineering", "nerf", "fun"),
            viewCountText = "75M views"
        ),
        VideoItem(
            id = "21RK2P20-z0",
            title = "Pixel-Perfect Minecraft Grass Cake ⚡",
            channelTitle = "Rosanna Pansino",
            description = "Baking a pixel-perfect square chocolate fudge cake layered with green matcha grass icing.",
            category = VideoCategory.SHORTS,
            ageGroup = AgeGroup.TEEN,
            durationText = "0:35",
            isShort = true,
            tags = listOf("shorts", "minecraft", "cake", "baking", "food"),
            viewCountText = "34M views"
        )
    )
}
