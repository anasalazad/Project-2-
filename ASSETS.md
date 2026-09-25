# The Feline Co. — Image & Font Shopping List

Everything below is **optional for building**. The app runs without any of it and shows a branded
placeholder (crimson gradient + paw) wherever an image is missing. Each file you add shows up
automatically the next time you run the app. No code changes are needed.

## Where to put the files

```
app/src/main/res/drawable-nodpi/     ← all photos (cats, products, banners)
app/src/main/res/font/               ← optional font files
```

## Rules (Android is strict about these)

- File names must be **exactly** as listed: lowercase letters, numbers and underscores only.
  No spaces, dashes or capitals.
- Use **`.jpg`** (or `.png` / `.webp`) and keep the name before the extension exactly as listed.
- Keep each photo roughly **1000–1600 px** wide and **under ~400 KB** (use https://squoosh.app to
  shrink them if needed). Very large photos slow the tablet down.
- Cat photos look best as **portrait or square** with the cat centred. Banners should be **landscape**.
- Free-to-use sources: **https://unsplash.com**, **https://www.pexels.com**, **https://pixabay.com**.

---

## 1. Banners (3 files)

| File name | Used on | Search for | Look for |
|---|---|---|---|
| `hero_home.jpg` | Home screen hero | "cat dark background portrait" | Moody, dark or black background, cat looking at camera, landscape |
| `banner_seniors.jpg` | "Cats 2+ adopt free" banner | "old cat relaxing" | Calm older cat lounging, landscape |
| `login_background.jpg` | Login / Register | "black cat close up" | Dramatic close-up, lots of dark space |

## 2. Cats (24 files)

**Kittens (under 6 months, 150 credits)**

| File name | Cat | Search for | Look for |
|---|---|---|---|
| `cat_mochi.jpg` | Mochi, 3 mo, female, Ragdoll mix | "ragdoll kitten" | Fluffy cream kitten with dark face/ears, blue eyes |
| `cat_biscuit.jpg` | Biscuit, 4 mo, male, Domestic Shorthair | "orange tabby kitten" | Ginger striped kitten |
| `cat_pepper.jpg` | Pepper, 2 mo, female, Domestic Shorthair | "black kitten" | Tiny all-black kitten |
| `cat_sushi.jpg` | Sushi, 5 mo, male, British Shorthair | "british shorthair kitten" | Round-faced grey-blue kitten |
| `cat_clover.jpg` | Clover, 3 mo, female, Domestic Mediumhair | "calico kitten" | White, orange and black patched kitten |
| `cat_nugget.jpg` | Nugget, 4 mo, male, Siamese mix | "siamese kitten" | Cream kitten with brown points, blue eyes |

**Young cats (6 months – 2 years, 100 credits)**

| File name | Cat | Search for | Look for |
|---|---|---|---|
| `cat_luna.jpg` | Luna, 10 mo, female, Russian Blue | "russian blue cat" | Sleek silver-grey cat, green eyes |
| `cat_milo.jpg` | Milo, 14 mo, male, Bengal | "bengal cat" | Leopard-spotted golden-brown cat |
| `cat_hazel.jpg` | Hazel, 8 mo, female, Domestic Shorthair | "brown tabby cat young" | Brown striped young cat |
| `cat_oreo.jpg` | Oreo, 18 mo, male, Domestic Shorthair | "tuxedo cat" | Black cat with a white chest and paws |
| `cat_willow.jpg` | Willow, 20 mo, female, Maine Coon | "maine coon cat" | Big fluffy brown tabby with ear tufts |
| `cat_ziggy.jpg` | Ziggy, 12 mo, male, Sphynx | "sphynx cat" | Hairless cat |

**Adult & senior cats (2 years and over, FREE)**

| File name | Cat | Search for | Look for |
|---|---|---|---|
| `cat_duchess.jpg` | Duchess, 5 yr, female, Persian | "white persian cat" | Long-haired white cat, flat face |
| `cat_winston.jpg` | Winston, 7 yr, male, British Shorthair | "british shorthair cat" | Chunky grey-blue adult cat, copper eyes |
| `cat_ginger.jpg` | Ginger, 3 yr, female, Domestic Shorthair | "ginger and white cat" | Orange and white cat |
| `cat_shadow.jpg` | Shadow, 4 yr, male, Bombay | "black cat yellow eyes" | Glossy black cat, amber eyes |
| `cat_cleo.jpg` | Cleo, 6 yr, female, Abyssinian | "abyssinian cat" | Slim ruddy/ticked coat, big ears |
| `cat_smokey.jpg` | Smokey, 9 yr, male, Domestic Longhair | "grey long haired cat" | Fluffy smoke-grey cat |
| `cat_pumpkin.jpg` | Pumpkin, 2.5 yr, male, Domestic Shorthair | "orange tabby cat" | Adult ginger tabby |
| `cat_bella.jpg` | Bella, 11 yr, female, Tortoiseshell | "tortoiseshell cat" | Mottled black and orange cat |
| `cat_archie.jpg` | Archie, 13 yr, male, Maine Coon | "old maine coon" / "red maine coon" | Large red/ginger long-haired cat |
| `cat_nala.jpg` | Nala, 3 yr, female, Birman | "birman cat" | Cream long-hair with dark points and white "gloves" |
| `cat_felix.jpg` | Felix, 8 yr, male, Domestic Shorthair | "grey tabby cat" | Grey striped adult cat |
| `cat_olive.jpg` | Olive, 15 yr, female, Domestic Shorthair | "old tabby cat" / "senior cat" | Older tabby-and-white cat, gentle look |

## 3. Shop products (18 files)

These can be real product photos or plain photos of the item type. Photos on a plain white or
light background look most professional. "Royal Feline" is our made-up house brand, so any generic
bag of cat food works.

| File name | Product | Search for |
|---|---|---|
| `product_kitten_formula.jpg` | Royal Feline Kitten Formula, dry 2 kg | "cat food bag" / "kitten food" |
| `product_adult_indoor.jpg` | Royal Feline Adult Indoor, dry 4 kg | "dry cat food bag" |
| `product_senior_formula.jpg` | Royal Feline Senior 11+, dry 2 kg | "cat food kibble bowl" |
| `product_wet_pouches.jpg` | Royal Feline Sensitive Wet Pouches ×12 | "wet cat food pouch" |
| `product_salmon_pate.jpg` | Royal Feline Salmon Pâté ×6 tins | "cat food tin" |
| `product_chicken_bites.jpg` | Freeze-Dried Chicken Bites | "cat treats" |
| `product_lickable_treats.jpg` | Salmon Lickable Treats | "lickable cat treat" / "cat treat tube" |
| `product_feather_wand.jpg` | Feather Wand Teaser | "cat feather toy" |
| `product_catnip_mice.jpg` | Catnip Mice (3 pack) | "toy mouse cat" |
| `product_ball_tower.jpg` | Interactive Ball Track Tower | "cat ball track toy" |
| `product_play_tunnel.jpg` | Crinkle Play Tunnel | "cat tunnel" |
| `product_donut_bed.jpg` | Plush Donut Calming Bed | "donut cat bed" |
| `product_window_perch.jpg` | Window Perch Hammock | "cat window perch" |
| `product_scratching_post.jpg` | Sisal Scratching Post 80 cm | "cat scratching post" |
| `product_cat_tree.jpg` | Deluxe Cat Tree 150 cm | "cat tree" |
| `product_travel_carrier.jpg` | Hard-Shell Travel Carrier | "cat carrier" |
| `product_water_fountain.jpg` | Stainless Water Fountain | "cat water fountain" |
| `product_slicker_brush.jpg` | Self-Cleaning Slicker Brush | "cat grooming brush" |

## 4. Fonts (optional, 3 files)

These make the app look more premium. Without them, system fonts are used.
Download them from https://fonts.google.com, open each family, click **Get font → Download all**, and
copy the **static** files listed here. Rename each one exactly as shown.

| File name | From family | Original static file |
|---|---|---|
| `playfair_display_bold.ttf` | Playfair Display | `PlayfairDisplay-Bold.ttf` |
| `nunito_regular.ttf` | Nunito | `Nunito-Regular.ttf` |
| `nunito_bold.ttf` | Nunito | `Nunito-Bold.ttf` |

Put them in `app/src/main/res/font/` (create the folder if it does not exist).

## Checklist
- [ ] 3 banners
- [ ] 24 cats
- [ ] 18 products
- [ ] 3 fonts (optional)
