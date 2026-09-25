package com.thefelineco.data.local.seed

import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.CoatLength.HAIRLESS
import com.thefelineco.domain.model.CoatLength.LONG
import com.thefelineco.domain.model.CoatLength.MEDIUM
import com.thefelineco.domain.model.CoatLength.SHORT
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.ProductCategory
import com.thefelineco.domain.model.Sex
import com.thefelineco.domain.model.Sex.FEMALE
import com.thefelineco.domain.model.Sex.MALE

/**
 * Starting content for a fresh install. Image names match the files listed in ASSETS.md.
 */
object SeedData {

    /** Small builder so each cat below reads like a profile rather than a constructor call. */
    private fun cat(
        name: String,
        breed: String,
        ageMonths: Int,
        sex: Sex,
        coat: CoatLength,
        colour: String,
        weightKg: Double,
        personality: List<String>,
        kids: Boolean,
        cats: Boolean,
        dogs: Boolean,
        indoor: Boolean,
        description: String,
        specialNeeds: String? = null,
        status: AdoptionStatus = AdoptionStatus.AVAILABLE,
    ) = Cat(
        name = name, breed = breed, ageMonths = ageMonths, sex = sex, coat = coat, colour = colour,
        weightKg = weightKg, personality = personality, description = description,
        imageName = "cat_${name.lowercase()}", goodWithKids = kids, goodWithCats = cats,
        goodWithDogs = dogs, indoorOnly = indoor, specialNeeds = specialNeeds, status = status,
    )

    val cats: List<Cat> = listOf(
        // Kittens
        cat(
            "Mochi", "Ragdoll mix", 3, FEMALE, LONG, "Seal point", 1.4,
            listOf("Cuddly", "Gentle", "Curious"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Mochi is a cloud of cream fluff with sapphire eyes. She goes floppy the moment " +
                "you pick her up, true to her Ragdoll roots, and will happily nap on your lap while you work.",
        ),
        cat(
            "Biscuit", "Domestic Shorthair", 4, MALE, SHORT, "Orange tabby", 1.8,
            listOf("Playful", "Confident", "Foodie"), kids = true, cats = true, dogs = true, indoor = false,
            description = "Biscuit is a ginger bundle of mischief who has never met a feather wand he didn't " +
                "want to conquer. He's bold, friendly and would thrive in a busy, fun-loving home.",
        ),
        cat(
            "Pepper", "Domestic Shorthair", 2, FEMALE, SHORT, "Black", 0.9,
            listOf("Sweet", "Chatty", "Cuddly"), kids = true, cats = true, dogs = false, indoor = true,
            description = "Tiny Pepper is all shine and sass. She chirps to say hello, follows you from room " +
                "to room and falls asleep tucked under your chin.",
        ),
        cat(
            "Sushi", "British Shorthair", 5, MALE, SHORT, "Blue", 2.1,
            listOf("Calm", "Easygoing", "Cuddly"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Sushi has the round cheeks and teddy-bear coat his breed is famous for. He is " +
                "unusually relaxed for a kitten and would suit a first-time owner perfectly.",
        ),
        cat(
            "Clover", "Domestic Mediumhair", 3, FEMALE, MEDIUM, "Calico", 1.3,
            listOf("Adventurous", "Playful", "Smart"), kids = true, cats = true, dogs = false, indoor = false,
            description = "Clover is a patchwork princess with a big personality. She loves puzzle feeders " +
                "and climbing to the highest point in the room to survey her kingdom.",
        ),
        cat(
            "Nugget", "Siamese mix", 4, MALE, SHORT, "Chocolate point", 1.6,
            listOf("Vocal", "Affectionate", "Clever"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Nugget will tell you all about his day. This talkative little Siamese mix bonds " +
                "deeply with his people and wants to be involved in everything you do.",
        ),

        // Young cats
        cat(
            "Luna", "Russian Blue", 10, FEMALE, SHORT, "Silver blue", 3.2,
            listOf("Gentle", "Shy", "Loyal"), kids = false, cats = true, dogs = false, indoor = true,
            description = "Luna is elegant and a little reserved at first, but once she trusts you she is " +
                "devoted. She would love a quiet, adult home where she can blossom at her own pace.",
        ),
        cat(
            "Milo", "Bengal", 14, MALE, SHORT, "Brown spotted", 4.6,
            listOf("Energetic", "Athletic", "Smart"), kids = true, cats = false, dogs = true, indoor = true,
            description = "Milo is a mini leopard with endless energy. He plays fetch, loves water and needs " +
                "an owner who is keen on daily play sessions and tall cat trees.",
        ),
        cat(
            "Hazel", "Domestic Shorthair", 8, FEMALE, SHORT, "Brown tabby", 3.0,
            listOf("Friendly", "Playful", "Easygoing"), kids = true, cats = true, dogs = true, indoor = false,
            description = "Hazel is the definition of an all-rounder. She is friendly with everyone she meets " +
                "and is equally happy chasing toys or curling up for a nap in a sunny spot.",
        ),
        cat(
            "Oreo", "Domestic Shorthair", 18, MALE, SHORT, "Black & white tuxedo", 4.8,
            listOf("Charming", "Social", "Foodie"), kids = true, cats = true, dogs = true, indoor = false,
            description = "Always dressed for the occasion, Oreo is a dapper gentleman who greets visitors at " +
                "the door. He loves treats a little too much and will perform tricks for them.",
        ),
        cat(
            "Willow", "Maine Coon", 20, FEMALE, LONG, "Brown tabby", 5.4,
            listOf("Gentle giant", "Affectionate", "Calm"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Willow is still growing into her magnificent ear tufts and plume of a tail. She is " +
                "gentle with children and makes soft trilling sounds when she's happy.",
        ),
        cat(
            "Ziggy", "Sphynx", 12, MALE, HAIRLESS, "Pink & grey", 3.6,
            listOf("Attention-seeker", "Warm", "Goofy"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Ziggy is a velvety, warm-blooded hot water bottle with the personality of a clown. " +
                "He needs weekly baths and a cosy jumper in winter.",
            specialNeeds = "Indoor only; weekly bath for skin care",
        ),

        // Adults & seniors (free)
        cat(
            "Duchess", "Persian", 60, FEMALE, LONG, "White", 4.1,
            listOf("Regal", "Calm", "Lap cat"), kids = false, cats = true, dogs = false, indoor = true,
            description = "True to her name, Duchess expects to be treated like royalty. She loves being " +
                "brushed, lounging on velvet cushions and quiet evenings in.",
            specialNeeds = "Daily brushing",
        ),
        cat(
            "Winston", "British Shorthair", 84, MALE, SHORT, "Blue", 6.2,
            listOf("Laid-back", "Dignified", "Cuddly"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Winston is a distinguished gentleman with copper eyes and a plush coat. He enjoys " +
                "a good routine, a warm lap and supervising from the couch.",
        ),
        cat(
            "Ginger", "Domestic Shorthair", 36, FEMALE, SHORT, "Ginger & white", 4.0,
            listOf("Friendly", "Chatty", "Curious"), kids = true, cats = true, dogs = true, indoor = false,
            description = "Ginger is a rare female ginger and a total social butterfly. She loves chatting, " +
                "exploring the garden and getting a chin scratch at the end of the day.",
        ),
        cat(
            "Shadow", "Bombay", 48, MALE, SHORT, "Black", 5.0,
            listOf("Loyal", "Playful", "Velcro cat"), kids = true, cats = false, dogs = true, indoor = true,
            description = "Shadow lives up to his name and will follow you everywhere. His glossy coat and " +
                "amber eyes make him look like a mini panther with a big, soft heart.",
        ),
        cat(
            "Cleo", "Abyssinian", 72, FEMALE, SHORT, "Ruddy", 3.8,
            listOf("Active", "Intelligent", "Curious"), kids = true, cats = true, dogs = false, indoor = true,
            description = "Cleo is an elegant athlete who loves climbing, puzzle toys and learning tricks. " +
                "She'd suit an engaged owner who enjoys an interactive cat.",
        ),
        cat(
            "Smokey", "Domestic Longhair", 108, MALE, LONG, "Smoke grey", 5.6,
            listOf("Mellow", "Gentle", "Lap cat"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Smokey is a big, gentle soul with a luxurious coat. At nine, he's all about naps, " +
                "brushing sessions and being the calm presence in your home.",
        ),
        cat(
            "Pumpkin", "Domestic Shorthair", 30, MALE, SHORT, "Orange tabby", 5.2,
            listOf("Goofy", "Friendly", "Foodie"), kids = true, cats = true, dogs = true, indoor = false,
            description = "Pumpkin is a classic ginger boy: loud purr, big appetite and absolutely no shame. " +
                "He gets along with everyone and makes every day funnier.",
        ),
        cat(
            "Bella", "Domestic Shorthair", 132, FEMALE, SHORT, "Tortoiseshell", 3.9,
            listOf("Independent", "Sweet", "Quiet"), kids = false, cats = false, dogs = false, indoor = true,
            description = "Bella is a gentle senior tortie who wants to be your only pet. She enjoys a sunny " +
                "window, a soft blanket and quiet company.",
        ),
        cat(
            "Archie", "Maine Coon", 156, MALE, LONG, "Red tabby", 7.4,
            listOf("Gentle giant", "Wise", "Affectionate"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Archie is a magnificent, lion-like senior with a heart to match. He's slowed down a " +
                "little but still loves head bumps and following you to the kitchen.",
            specialNeeds = "Joint supplement daily",
        ),
        cat(
            "Nala", "Birman", 36, FEMALE, LONG, "Seal point", 4.2,
            listOf("Sweet", "Social", "Gentle"), kids = true, cats = true, dogs = true, indoor = true,
            description = "Nala has silky fur, sapphire eyes and perfect white 'gloves'. She is a soft, " +
                "sociable companion who loves being part of family life.",
        ),
        cat(
            "Felix", "Domestic Shorthair", 96, MALE, SHORT, "Grey tabby", 5.3,
            listOf("Easygoing", "Loyal", "Cuddly"), kids = true, cats = true, dogs = true, indoor = false,
            description = "Felix is a steady, loving companion who takes life as it comes. He enjoys a bit of " +
                "garden time and then a long nap on the end of your bed.",
        ),
        cat(
            "Olive", "Domestic Shorthair", 180, FEMALE, SHORT, "Tabby & white", 3.5,
            listOf("Gentle", "Affectionate", "Quiet"), kids = true, cats = true, dogs = false, indoor = true,
            description = "At fifteen, Olive is our wise old queen. She is gentle, purrs the moment you touch " +
                "her and wants nothing more than a warm spot to spend her golden years.",
            specialNeeds = "Kidney-friendly diet",
        ),
    )

    private fun product(
        name: String,
        brand: String,
        category: ProductCategory,
        price: Int,
        image: String,
        stock: Int,
        description: String,
    ) = Product(
        name = name, brand = brand, category = category, priceCredits = price,
        description = description, imageName = "product_$image", stock = stock,
    )

    private const val HOUSE = "Royal Feline"
    private const val COLLECTION = "The Feline Co."

    val products: List<Product> = listOf(
        product(
            "Kitten Formula Dry Food 2kg", HOUSE, ProductCategory.FOOD, 60, "kitten_formula", 40,
            "Complete nutrition for kittens up to 12 months, with DHA for healthy brain and eye development.",
        ),
        product(
            "Adult Indoor Dry Food 4kg", HOUSE, ProductCategory.FOOD, 80, "adult_indoor", 35,
            "Balanced recipe for indoor cats with fibre to reduce hairballs and help keep a healthy weight.",
        ),
        product(
            "Senior 11+ Dry Food 2kg", HOUSE, ProductCategory.FOOD, 65, "senior_formula", 25,
            "Gentle on ageing kidneys and joints, with softer kibble for mature mouths.",
        ),
        product(
            "Sensitive Digestion Wet Pouches ×12", HOUSE, ProductCategory.FOOD, 45, "wet_pouches", 50,
            "Tender chunks in gravy made with highly digestible proteins for sensitive tummies.",
        ),
        product(
            "Salmon Pâté ×6 Tins", HOUSE, ProductCategory.FOOD, 35, "salmon_pate", 60,
            "Smooth, protein-rich salmon pâté. A favourite with fussy eaters.",
        ),
        product(
            "Freeze-Dried Chicken Bites", HOUSE, ProductCategory.TREATS, 20, "chicken_bites", 70,
            "Single-ingredient, 100% chicken breast treats. Perfect for training and bonding.",
        ),
        product(
            "Salmon Lickable Treats ×8", HOUSE, ProductCategory.TREATS, 18, "lickable_treats", 80,
            "Creamy squeeze-up treats to hand-feed, or to hide tablets in at medicine time.",
        ),
        product(
            "Feather Wand Teaser", COLLECTION, ProductCategory.TOYS, 15, "feather_wand", 45,
            "Telescopic wand with swappable feather lures that brings out the hunter in every cat.",
        ),
        product(
            "Catnip Mice (3 pack)", COLLECTION, ProductCategory.TOYS, 12, "catnip_mice", 90,
            "Plush mice filled with organic catnip for bursts of playful energy.",
        ),
        product(
            "Interactive Ball Track Tower", COLLECTION, ProductCategory.TOYS, 30, "ball_tower", 20,
            "Three levels of spinning balls to keep solo cats busy while you're out.",
        ),
        product(
            "Crinkle Play Tunnel", COLLECTION, ProductCategory.TOYS, 28, "play_tunnel", 18,
            "Collapsible three-way tunnel with a crinkly lining and a dangling pom-pom.",
        ),
        product(
            "Plush Donut Calming Bed", COLLECTION, ProductCategory.BEDS, 55, "donut_bed", 15,
            "Ultra-soft faux fur with raised edges that help anxious cats feel safe and secure.",
        ),
        product(
            "Window Perch Hammock", COLLECTION, ProductCategory.BEDS, 48, "window_perch", 12,
            "Suction-mounted perch that holds up to 18 kg. Front-row seats to the bird show.",
        ),
        product(
            "Sisal Scratching Post 80cm", COLLECTION, ProductCategory.ACCESSORIES, 40, "scratching_post", 22,
            "Tall, heavy-based sisal post to save your sofa, with a plush top for lounging.",
        ),
        product(
            "Deluxe Cat Tree 150cm", COLLECTION, ProductCategory.ACCESSORIES, 120, "cat_tree", 6,
            "Five levels with condos, hammocks and scratching posts. A palace for your feline.",
        ),
        product(
            "Hard-Shell Travel Carrier", COLLECTION, ProductCategory.ACCESSORIES, 70, "travel_carrier", 14,
            "Airline-style carrier with a top-loading door, perfect for bringing your new cat home.",
        ),
        product(
            "Stainless Water Fountain", COLLECTION, ProductCategory.ACCESSORIES, 58, "water_fountain", 3,
            "Whisper-quiet filtered fountain that encourages cats to drink more water.",
        ),
        product(
            "Self-Cleaning Slicker Brush", COLLECTION, ProductCategory.GROOMING, 22, "slicker_brush", 0,
            "Removes loose fur and tangles, then clears with one click. Ideal for long-haired cats.",
        ),
    )
}
