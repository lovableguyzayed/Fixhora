# Builds FixoraX_UI_UX_Design.html from the real app screenshots in img/. Then render to PDF (see make_pdf.js).
import html, datetime
DATE = "13 November 2026"
E = html.escape

def phone(img, cap=None, w=190):
    c = f'<figcaption>{E(cap)}</figcaption>' if cap else ''
    return f'<figure class="ph" style="width:{w}px"><img src="img/{img}.jpg" alt="{E(cap or img)}">{c}</figure>'

def screen_page(title, img, purpose, elements, states=None, notes=None, extra_imgs=()):
    li = ''.join(f'<li>{e}</li>' for e in elements)
    st = f'<h4>States</h4><ul>{"".join(f"<li>{s}</li>" for s in states)}</ul>' if states else ''
    nt = f'<h4>Design notes</h4><ul>{"".join(f"<li>{n}</li>" for n in notes)}</ul>' if notes else ''
    ew = 210 if len(extra_imgs) > 1 else 270
    ex = ''.join(phone(i, c, ew) for i, c in extra_imgs)
    return f'''<section class="page screen"><h2>{E(title)}</h2>
<div class="row"><div class="phones">{phone(img, None, 300)}{ex}</div>
<div class="desc"><p class="purpose">{purpose}</p><h4>What is on the screen</h4><ul>{li}</ul>{st}{nt}</div></div></section>'''

P = []
# ---------------- cover ----------------
P.append(f'''<section class="page cover">
<img class="logo" src="img/logo.png" alt="">
<div class="brand">Fixora<span>X</span></div>
<h1>UI / UX Design Document</h1>
<p class="sub">Android app · package <b>in.fixorax.app</b> · version 1.0</p>
<p class="sub">Find trusted local workers or offer your skills to people nearby.</p>
<div class="covergrid">{phone("welcome_360x800",None,170)}{phone("customer_home_360x800",None,170)}{phone("my_tasks_360x800",None,170)}{phone("helper_home_360x800",None,170)}{phone("account_360x800",None,170)}</div>
<p class="meta">{DATE} · All screens in this document are real screenshots of the current app code (test mode, English, 360 × 800 dp phone unless stated).</p>
</section>''')

# ---------------- contents ----------------
toc = ["Product overview & users","Design principles","Design system: colour","Design system: type, spacing, shape","Components","App map (navigation)","Key user flows","Screens: login","Screens: customer (“I need help”)","Screens: helper (“I want to help”)","Screens: chat & account","States: empty, error, offline, guest","Small phones, large font, tablets","Hindi","Accessibility","Before → after (audit)","Open design items"]
P.append('<section class="page"><h2>Contents</h2><ol class="toc">'+''.join(f'<li>{E(t)}</li>' for t in toc)+'</ol></section>')

# ---------------- overview ----------------
P.append('''<section class="page"><h2>1. Product overview &amp; users</h2>
<div class="cols2"><div>
<p><b>FixoraX</b> is a local-services marketplace for India. One app, two sides:</p>
<ul><li><b>Customer — “I need help”:</b> posts a task (category, address, details, budget in ₹), gets offers from nearby helpers, chooses one, chats, marks it done.</li>
<li><b>Helper — “I want to help”:</b> sees open jobs, sends an offer (own price) or accepts at the customer’s price, chats once chosen.</li></ul>
<p>Login is by <b>mobile number + OTP</b> or <b>Google</b>. Guests may browse; posting or offering asks them to log in. Tasks, offers and chats are shared between phones through Firebase (Cloud Firestore) once the project is set up.</p>
<h3>Design target</h3>
<p>Bharat-first: Tier-2/3 towns, budget Android phones (3–4 GB RAM), weak networks, users who prefer Hindi or Hinglish, and older users with large font sizes.</p>
</div><div>
<h3>Personas</h3>
<table class="t"><tr><th>Persona</th><th>Needs from the design</th></tr>
<tr><td><b>Ramesh ji, 52</b> — kirana owner, small town, Redmi 3 GB, font size at max, Hindi-first</td><td>Big text and buttons, no passwords, plain words, no dead ends, works on weak Jio signal</td></tr>
<tr><td><b>Priya, 21</b> — student, Tier-2 city, price-sensitive, Google login, dark mode on</td><td>Fast Google sign-in, clear prices in ₹, readable in dark mode, shareable</td></tr>
<tr><td><b>Amit, 34</b> — salaried, metro, impatient</td><td>Few taps, instant feedback, polished feel</td></tr>
<tr><td><b>Shared family phone</b> — dual SIM</td><td>Pick SIM number, easy log out, data wiped on log out</td></tr></table>
</div></div></section>''')

P.append('''<section class="page"><h2>2. Design principles</h2>
<div class="cards">
<div class="card"><h3>1 · One clear action per screen</h3><p>Each screen has one blue primary button (56 dp tall). Secondary actions are outlined or text buttons.</p></div>
<div class="card"><h3>2 · Plain, honest words</h3><p>“Enter OTP”, “Send offer”, “Wrong OTP… 2 tries left”. No jargon such as “Authenticate”. No fake numbers, ratings or badges.</p></div>
<div class="card"><h3>3 · No passwords</h3><p>Mobile number + OTP or Google. New and returning users use the same screens; the account is created automatically.</p></div>
<div class="card"><h3>4 · Readable for everyone</h3><p>Navy text on white (16:1). Minimum text 12 sp. Layouts grow with the phone’s font size instead of clipping.</p></div>
<div class="card"><h3>5 · Works on weak networks</h3><p>Changes are saved on the phone at once and sent when the network returns; lists show the last known data; buttons never spin forever.</p></div>
<div class="card"><h3>6 · Indian formats</h3><p>+91 always visible, number cleaned automatically, ₹ with Indian grouping (₹1,25,000), dates DD/MM/YYYY in IST.</p></div>
<div class="card"><h3>7 · Native Android feel</h3><p>Material 3 components, edge-to-edge with safe areas, system Back closes sheets/chats first, “Press back again to exit” on the first tab.</p></div>
<div class="card"><h3>8 · Hindi-ready</h3><p>All texts live in one file (strings.xml). A Hindi version is drafted for review; layouts were checked with it.</p></div>
</div></section>''')

# ---------------- colour ----------------
sw = [("#0B57FF","Primary Blue","Primary buttons, links, selected tab, progress","White text on it: 5.5:1 ✓"),
      ("#FF6B00","Orange","Brand “X”, helper accents, Post-a-task button","White text on it: 2.9:1 ✗ (see open items)"),
      ("#0B1F44","Dark Navy","Titles and main text","On white: 16.2:1 ✓"),
      ("#6B7280","Text Grey","Secondary text, hints, timestamps","On white: 4.8:1 ✓"),
      ("#E5E7EB","Border Grey","Card and field borders, dividers","Decorative"),
      ("#FFFFFF","White","Screens, cards","—"),
      ("#F8F9FA","Page Grey","Background behind card lists","Grey text on it: 4.6:1 ✓"),
      ("#C62828","Error Red","Errors, Delete account","On white: 5.6:1 ✓")]
sem = [("New / success","#E8F5E9","#2E7D32","4.6:1 ✓"),("Waiting / offers","#FFF3E0","#B45309","4.6:1 ✓"),("Chosen / accepted","#E3F2FD","#0B57FF","4.8:1 ✓"),("Cancelled / not chosen","#F1F3F5","#6B7280","4.35:1 ✗ small")]
P.append('<section class="page"><h2>3. Design system: colour</h2><div class="swatches">'+''.join(
  f'<div class="sw"><div class="chip" style="background:{h};{"border:1px solid #E5E7EB" if h in ("#FFFFFF","#F8F9FA","#E5E7EB") else ""}"></div><b>{n}</b><code>{h}</code><span>{u}</span><em>{c}</em></div>' for h,n,u,c in sw)+
  '</div><h3>Status badges</h3><div class="badges">'+''.join(f'<span class="bd" style="background:{bg};color:{fg}">{n}</span><small>{fg} on {bg} · {c}</small>' for n,bg,fg,c in sem)+
  '</div><p class="note">Contrast is measured against the WCAG rule of at least 4.5:1 for normal text (readable in bright sunlight). The app is always shown in its light theme; on phones in dark mode the status-bar icons stay dark and OEM “force dark” is switched off, so colours never invert.</p></section>')

# ---------------- type/spacing ----------------
P.append('''<section class="page"><h2>4. Design system: type, spacing, shape</h2><div class="cols2"><div>
<h3>Type scale (Roboto, system font)</h3>
<table class="t type"><tr><th>Use</th><th>Size / weight</th><th>Sample</th></tr>
<tr><td>Screen title</td><td>26 sp · Bold</td><td style="font-size:26px;font-weight:700">Enter OTP</td></tr>
<tr><td>Tab title</td><td>22 sp · Bold</td><td style="font-size:22px;font-weight:700">My tasks</td></tr>
<tr><td>Brand</td><td>28–48 sp · ExtraBold italic</td><td style="font-size:26px;font-weight:800;font-style:italic;color:#0B57FF">Fixora<span style="color:#FF6B00">X</span></td></tr>
<tr><td>Card title</td><td>16 sp · Bold</td><td style="font-size:16px;font-weight:700">Kitchen tap is leaking</td></tr>
<tr><td>Body / button</td><td>15–16 sp · Regular / SemiBold</td><td style="font-size:16px">Continue with mobile number</td></tr>
<tr><td>Secondary</td><td>13–14 sp · Regular, grey</td><td style="font-size:14px;color:#6B7280">Civil Lines, Kanpur · Budget ₹300 – ₹800</td></tr>
<tr><td>Smallest (badges, time)</td><td>12 sp · Bold / Regular</td><td style="font-size:12px;font-weight:700;color:#2E7D32">New</td></tr></table>
<p class="note">No text is smaller than 12 sp. All sizes scale with the phone’s font-size setting (tested at 200 %).</p>
</div><div>
<h3>Spacing (4/8-point grid)</h3>
<table class="t"><tr><th>Token</th><th>Value</th><th>Used for</th></tr>
<tr><td>xs</td><td>4 dp</td><td>Icon–text gaps</td></tr><tr><td>s</td><td>8 dp</td><td>Between chips, small gaps</td></tr>
<tr><td>m</td><td>12 dp</td><td>Between cards</td></tr><tr><td>l</td><td>16 dp</td><td>Card padding, list edges</td></tr>
<tr><td>xl</td><td>24 dp</td><td>Screen side padding (login screens)</td></tr></table>
<h3>Shape &amp; size</h3>
<table class="t"><tr><th>Element</th><th>Rule</th></tr>
<tr><td>Buttons, fields</td><td>12 dp corners · min height 56 dp (48 dp in cards)</td></tr>
<tr><td>Cards</td><td>16 dp corners · 1 dp Border Grey · no heavy shadows</td></tr>
<tr><td>Role cards</td><td>24 dp corners</td></tr><tr><td>Badges</td><td>8 dp corners</td></tr>
<tr><td>Touch targets</td><td>at least 48 × 48 dp</td></tr>
<tr><td>Max content width</td><td>640 dp, centred (tablets, foldables, landscape)</td></tr>
<tr><td>Icons</td><td>Material Icons (one family), 16–24 dp</td></tr></table>
</div></div></section>''')

# ---------------- components ----------------
P.append('''<section class="page"><h2>5. Components</h2><div class="comp">
<div class="cp"><h4>Primary button</h4><div class="btn p">Send OTP</div><p>Blue, white text, 56 dp, full width. Shows a spinner and disables itself while working (no double taps).</p></div>
<div class="cp"><h4>Secondary button</h4><div class="btn o"><span class="g">G</span> Continue with Google</div><p>White with grey border. Google button uses the official four-colour “G”.</p></div>
<div class="cp"><h4>Text button</h4><div class="btn t">Explore without login</div><p>For low-emphasis actions: Change number, Cancel task, Not now.</p></div>
<div class="cp"><h4>Mobile field</h4><div class="field"><b>+91</b>&nbsp; 98765 43210</div><p>+91 always visible. Cleans “+91”, “0”, spaces, dashes on type/paste. Must be 10 digits starting 6–9.</p></div>
<div class="cp"><h4>OTP input</h4><div class="otp"><i class="f">4</i><i class="f">8</i><i class="f">2</i><i class="n"></i><i></i><i></i></div><p>One input drawn as six boxes: typing, Backspace, paste and SMS auto-fill all work; boxes shrink to fit 320 dp phones.</p></div>
<div class="cp"><h4>Filter chips</h4><div><span class="fc on">Active (2)</span><span class="fc">Done (0)</span><span class="fc">Cancelled</span></div><p>Scroll sideways at large font sizes instead of squeezing.</p></div>
<div class="cp"><h4>Status badges</h4><div><span class="bd" style="background:#E8F5E9;color:#2E7D32">New</span> <span class="bd" style="background:#FFF3E0;color:#B45309">3 offers</span> <span class="bd" style="background:#E3F2FD;color:#0B57FF">Chosen</span></div><p>Text always says the status; colour is never the only signal.</p></div>
<div class="cp"><h4>Avatar</h4><div class="av">R</div><p>First letter of the person’s name — no stock photos or fake faces.</p></div>
<div class="cp"><h4>Bottom navigation</h4><div class="nav"><span class="on">⌂<br>Home</span><span>☰<br>My tasks</span><span>✉<br>Chats</span><span>☺<br>Account</span></div><p>Material 3 navigation bar, icon + label on every tab, tabs keep their scroll position.</p></div>
<div class="cp"><h4>Dialogs</h4><p>Used only for decisions that matter: choose a helper, cancel a task, delete account, log in to continue. Destructive actions use red and say exactly what will happen.</p></div>
<div class="cp"><h4>Bottom sheet</h4><p>“Send your offer”: price (₹, number pad) + optional message. System Back closes it.</p></div>
<div class="cp"><h4>Cards</h4><p>Task, job, offer and account cards: white, 16 dp corners, thin border, 16 dp padding; time and status on the first line, title second.</p></div>
</div></section>''')

# ---------------- app map ----------------
P.append('''<section class="page"><h2>6. App map (navigation)</h2>
<svg viewBox="0 0 1060 520" class="map" xmlns="http://www.w3.org/2000/svg" font-family="Inter, FreeSans, sans-serif" font-size="13">
<defs><marker id="a" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#6B7280"/></marker></defs>
<g fill="#fff" stroke="#0B57FF" stroke-width="1.5">
<rect x="10" y="40" width="110" height="40" rx="10"/><rect x="160" y="40" width="120" height="40" rx="10"/>
<rect x="320" y="10" width="120" height="40" rx="10"/><rect x="480" y="10" width="100" height="40" rx="10"/><rect x="620" y="10" width="100" height="40" rx="10"/>
<rect x="320" y="80" width="150" height="40" rx="10"/><rect x="760" y="40" width="130" height="40" rx="10"/></g>
<g fill="#0B1F44" text-anchor="middle">
<text x="65" y="65">Splash</text><text x="220" y="65">Welcome</text><text x="380" y="35">Mobile number</text><text x="530" y="35">OTP</text><text x="670" y="35">Name (1st time)</text>
<text x="395" y="105">Explore (guest)</text><text x="825" y="58">Choose side</text><text x="825" y="73" font-size="11" fill="#6B7280">(remembered)</text></g>
<g stroke="#6B7280" stroke-width="1.5" fill="none" marker-end="url(#a)">
<path d="M120,60 H158"/><path d="M280,52 L318,32"/><path d="M280,68 L318,96"/><path d="M440,30 H478"/><path d="M580,30 H618"/><path d="M720,30 L758,52"/><path d="M470,100 L758,68"/></g>
<text x="135" y="100" font-size="11" fill="#6B7280">already logged in → straight to the last used side</text>
<g><rect x="40" y="170" width="460" height="300" rx="14" fill="#EEF4FF" stroke="#0B57FF"/>
<text x="60" y="198" font-weight="700" fill="#0B57FF" font-size="15">Customer — “I need help”</text>
<g fill="#fff" stroke="#0B57FF"><rect x="60" y="215" width="95" height="36" rx="8"/><rect x="165" y="215" width="95" height="36" rx="8"/><rect x="270" y="215" width="95" height="36" rx="8"/><rect x="375" y="215" width="105" height="36" rx="8"/></g>
<g text-anchor="middle" fill="#0B1F44"><text x="107" y="238">Home</text><text x="212" y="238">My tasks</text><text x="317" y="238">Chats</text><text x="427" y="238">Account</text></g>
<text x="60" y="285" fill="#0B1F44">Post a task (4 steps):</text>
<g fill="#fff" stroke="#FF6B00"><rect x="60" y="298" width="95" height="34" rx="8"/><rect x="165" y="298" width="95" height="34" rx="8"/><rect x="270" y="298" width="95" height="34" rx="8"/><rect x="375" y="298" width="105" height="34" rx="8"/></g>
<g text-anchor="middle" fill="#0B1F44"><text x="107" y="320">1 Category</text><text x="212" y="320">2 Location</text><text x="317" y="320">3 Details</text><text x="427" y="320">4 Review → Post</text></g>
<text x="60" y="365" fill="#0B1F44">My tasks → offers → Choose helper → Chat → Mark as done</text>
<text x="60" y="390" fill="#0B1F44">Home category tile or “Post a task” button starts step 1</text>
<text x="60" y="415" fill="#6B7280">Guests: posting asks to log in first</text>
<text x="60" y="440" fill="#6B7280">Account: edit name · switch side · help · policies · log out · delete</text></g>
<g><rect x="540" y="170" width="480" height="300" rx="14" fill="#FFF5EC" stroke="#FF6B00"/>
<text x="560" y="198" font-weight="700" fill="#C2410C" font-size="15">Helper — “I want to help”</text>
<g fill="#fff" stroke="#FF6B00"><rect x="560" y="215" width="95" height="36" rx="8"/><rect x="665" y="215" width="95" height="36" rx="8"/><rect x="770" y="215" width="95" height="36" rx="8"/><rect x="875" y="215" width="105" height="36" rx="8"/></g>
<g text-anchor="middle" fill="#0B1F44"><text x="607" y="238">Home</text><text x="712" y="238">My jobs</text><text x="817" y="238">Chats</text><text x="927" y="238">Account</text></g>
<text x="560" y="285" fill="#0B1F44">Home: Online switch · counts · jobs near you</text>
<text x="560" y="310" fill="#0B1F44">Job card → Send offer (sheet) or Accept at customer’s price</text>
<text x="560" y="335" fill="#0B1F44">My jobs: Waiting / Chosen / Not chosen / Done → Chat</text>
<text x="560" y="360" fill="#6B7280">Guests: sending an offer asks to log in first</text>
<text x="560" y="385" fill="#6B7280">Back on Home: “Press back again to exit”</text></g>
<path d="M825,80 C700,130 400,130 270,168" stroke="#6B7280" stroke-width="1.5" fill="none" marker-end="url(#a)"/>
<path d="M825,80 L780,168" stroke="#6B7280" stroke-width="1.5" fill="none" marker-end="url(#a)"/>
<text x="400" y="505" fill="#6B7280" font-size="12">Account → “Switch to …” moves between the two sides without logging out.</text>
</svg></section>''')

# ---------------- flows ----------------
P.append('''<section class="page"><h2>7. Key user flows</h2><table class="t flow">
<tr><th>Flow</th><th>Steps</th><th>Taps*</th></tr>
<tr><td><b>First login (mobile)</b></td><td>Welcome → Continue with mobile number → pick SIM number (or type) → Send OTP → OTP fills itself from SMS (or type/paste) → Name → Continue → choose side</td><td>5–6</td></tr>
<tr><td><b>Login (Google)</b></td><td>Welcome → Continue with Google → pick account → choose side (name comes from Google)</td><td>3</td></tr>
<tr><td><b>Returning user</b></td><td>Open app → lands on the last used side, logged in</td><td>0</td></tr>
<tr><td><b>Post a task</b></td><td>Home: tap category → address → Continue → title (+ details, budget, photos optional) → Continue → Post task → “Task posted!” → See my tasks</td><td>5 + typing</td></tr>
<tr><td><b>Helper sends offer</b></td><td>Home: job card → Accept ₹800 (1 tap) — or Send offer → price + message → Send offer for ₹650</td><td>1–3</td></tr>
<tr><td><b>Customer chooses</b></td><td>My tasks → offer → Choose → confirm</td><td>2</td></tr>
<tr><td><b>Chat</b></td><td>My tasks / My jobs → Chat → type → Send (helpers also have quick replies)</td><td>2 + typing</td></tr>
<tr><td><b>Finish</b></td><td>Customer: My tasks → Mark as done</td><td>1</td></tr>
<tr><td><b>Log out / delete</b></td><td>Account → Log out (1 tap) · Delete my account → confirm (2 taps). Both wipe this phone’s data.</td><td>1–2</td></tr>
<tr><td><b>Wrong OTP</b></td><td>Message “Wrong OTP. Please check the SMS and try again. (2 tries left)”; after 3 wrong tries → “Tap Resend OTP”; resend after 30 s</td><td>—</td></tr>
</table><p class="note">* Taps counted from the screen where the flow starts; typing not counted.</p></section>''')

# ---------------- screens ----------------
S = []
S.append(screen_page("8.1 Welcome", "welcome_360x800",
  "First screen for new or logged-out users. Two equal ways to continue, plus browsing without login.",
  ["English / हिंदी choice at the top (Hindi in preparation)", "Illustration that shrinks on small phones", "“Welcome to FixoraX” and one-line promise",
   "<b>Continue with mobile number</b> (primary) and <b>Continue with Google</b> (official G logo)", "Explore without login", "Terms and Privacy line (links appear once the pages exist)"],
  ["Google busy: spinner in the Google button", "Google problem: plain message under the buttons (e.g. no Google account on the phone)"],
  ["Replaces the old Sign In / Create Account / password screens", "Whole screen scrolls at large font sizes"]))
S.append(screen_page("8.2 Mobile number", "mobile_login_360x800",
  "Enter or pick the mobile number to receive an OTP. Same screen for new and returning users.",
  ["+91 always visible", "Number pad keyboard; spaces, dashes, leading 0 and +91 removed automatically", "<b>Pick number from my SIM</b> — Google’s phone-number hint, no permission needed, shows both SIMs",
   "<b>Send OTP</b> with spinner while sending", "“No password needed.”"],
  ["Error under the field: “Enter your 10-digit mobile number” / “should start with 6, 7, 8 or 9”", "Too many tries, no internet: plain messages", "Test mode: yellow note “Use OTP 123456” (only until Firebase is set up)"]))
S.append(screen_page("8.3 Enter OTP", "otp_360x800",
  "Type, paste or auto-read the 6-digit code.",
  ["“Sent by SMS to +91 98765 43210” and <b>Change number</b>", "Six OTP boxes (one real input)", "Resend OTP in 30 s, then <b>Resend OTP</b>", "Verify — checks automatically when 6 digits are entered"],
  ["Auto-filled from SMS: green tick “OTP filled automatically from your SMS”", "Wrong OTP: red boxes and “2 tries left”; after 3 → resend required", "Expired code: “This OTP has expired. Tap Resend OTP”"],
  ["Survives the app being closed while the user reads the SMS (number and code request are saved)"],
  [("otp_wrong_360x800","Wrong OTP")]))
S.append(screen_page("8.4 Name", "name_360x800",
  "Asked only once, after the first login. The only profile question.",
  ["“What should we call you?”", "One field — a single name is fine (no surname required)", "Continue"],
  ["Less than 2 letters: “Please enter your name (at least 2 letters)”"],
  ["Replaces the old 7-field profile (gender, date of birth, state, PIN…)"]))
S.append(screen_page("8.5 Choose your side", "role_selection_360x800",
  "Shown once; the choice is remembered. Can be changed later from Account.",
  ["FixoraX logo", "Two large cards: <b>I need help</b> (blue) and <b>I want to help</b> (orange) with illustrations", "English / हिंदी pills (this screen already has its own Hindi)"],
  None, ["Cards grow with large text instead of cutting it off"]))
P.append('<section class="page divider"><h2>8. Screens: login</h2><p>Welcome → Mobile number → OTP → Name → Choose side</p></section>')
P += S; S = []

P.append('<section class="page divider"><h2>9. Screens: customer (“I need help”)</h2><p>Home · Post a task (4 steps) · My tasks</p></section>')
S.append(screen_page("9.1 Customer Home", "customer_home_360x800",
  "Start a task in one tap.",
  ["“Namaste, Ramesh”", "“What do you need help with?” — “Free to post.”", "Nine category tiles: Home Repairs, Cleaning, Moving Help, Tech Support, Errands, Painting, Car Services, Tutoring, More",
   "Tapping a tile opens the task form with that category already chosen"],
  ["Guest: tapping a tile shows “Log in to continue”"]))
S.append(screen_page("9.2 Post a task — 1 Category", "task_category_360x800",
  "Choose what kind of help is needed.",
  ["Step bar: Category · Location · Details · Review", "Search box filters categories", "Grid of category tiles (3 columns on phones, more on tablets, fewer at large font)", "Continue"],
  ["No category chosen: “Please select a category to continue”"], ["Heading and search scroll with the tiles, so small phones see more tiles"]))
S.append(screen_page("9.3 Post a task — 2 Location", "task_location_360x800",
  "Where the help is needed.",
  ["Address field with suggestions (house no., area, landmark, city — e.g. “Near Hanuman Mandir”)", "Service area (within 5 km)", "“Your location is safe with us”", "Continue"],
  ["Empty address: “Please enter the address where you need help”"], ["The fake “use my current location” switch was removed; real GPS is an open decision (D7)"]))
S.append(screen_page("9.4 Post a task — 3 Details", "task_details_360x800",
  "Photos (optional), title, description and budget.",
  ["Upload photos (up to 5, from the gallery — no storage permission needed)", "Task title * and detailed description", "Budget range in ₹ (number pad, optional)", "Tips for better responses", "Continue"],
  ["No title: “Title is required to post the task”", "Max below min: “Max budget should be more than min budget”", "Photo could not be added: “Please check phone storage and try again”"],
  ["The ⚡ “sample photos for testing” link is visible only in test builds, never to real users"]))
S.append(screen_page("9.5 Post a task — 4 Review", "task_review_360x800",
  "Check everything before posting.",
  ["Cards for Category, Task, Location, Photos, Budget — each with Edit", "“You’re in control” reassurance", "<b>Post Task</b> (disabled while posting — no double posts)", "Then: “Task posted!” → See my tasks"],
  ["Posting failed: “Could not post your task. Please check your internet and try again.”"]))
S.append(screen_page("9.6 My tasks", "my_tasks_360x800",
  "Everything the customer has posted, with offers from helpers.",
  ["Chips: Active (2) · Done · Cancelled", "Task card: posted time, status badge (“3 offers”, “Waiting for offers”, “Helper chosen”), title, place, budget",
   "Offers cheapest first: name, message, price, <b>Choose</b> (asks to confirm)", "See all N offers · Cancel task (asks to confirm)", "Chosen helper: “Raju · ₹4,500 agreed”, <b>Mark as done</b>, <b>Chat</b>", "Orange <b>Post a task</b> button"],
  ["Empty: “No tasks yet — Post a task and helpers near you will send offers. Free to post.”"], None,
  [("my_tasks_empty_360x800","Empty state")]))
P += S; S = []

P.append('<section class="page divider"><h2>10. Screens: helper (“I want to help”)</h2><p>Home · My jobs</p></section>')
S.append(screen_page("10.1 Helper Home", "helper_home_360x800",
  "See open jobs nearby and send offers.",
  ["“Namaste, Suresh” and the <b>Online</b> switch (saved)", "Real counts: Jobs near you · Chosen · Completed", "“Ratings and earnings will show here after your first completed job.” (no fake numbers)",
   "Job card: category, time, New badge, title, details, place, budget", "<b>Send offer</b> (own price + message) or <b>Accept ₹800</b> (customer’s price)", "After sending: “Offer sent: ₹2,000 · waiting for customer”"],
  ["Loading: grey skeleton cards", "Offline switch: “You are offline. Turn on Online to see new jobs.”", "No jobs: “New jobs appear here automatically.”", "Own task: “Posted by you”, no offer buttons"]))
S.append(screen_page("10.2 My jobs", "my_jobs_360x800",
  "Every offer the helper has sent and what happened.",
  ["Badges: Waiting · Chosen · Not chosen · Done", "Task title, place, “Your offer: ₹4,500”", "<b>Chat</b> when chosen"],
  ["Empty: “Offers you send appear here.” + See jobs near you"]))
P += S; S = []

P.append('<section class="page divider"><h2>11. Screens: chat &amp; account</h2></section>')
S.append(screen_page("11.1 Chats", "chats_list_360x800",
  "One conversation per task, between the customer and the chosen helper only.",
  ["List: avatar, other person’s name, task title", "Tap to open"],
  ["Empty (customer): “Chats appear here after you choose a helper.”", "Empty (helper): “Chats appear here after a customer chooses you.”"]))
S.append(screen_page("11.2 Conversation", "chat_conversation_360x800",
  "Simple messaging; works on weak networks (messages are sent when the network returns).",
  ["Header: name, task and agreed price", "My messages blue on the right, theirs white on the left, with IST time (e.g. 7:59 am)", "Message box with Send", "Helpers get quick replies (“I can do this today.”, “I’ll arrive in 20 minutes.”)"],
  None, ["System Back closes the conversation first"], [("chat_helper_360x800","Helper view with quick replies")]))
S.append(screen_page("11.3 Account", "account_360x800",
  "Profile and everything about the account in one place.",
  ["Profile card: initial avatar, name, number, <b>Edit</b>", "Language / भाषा", "Switch to “I want to help” / “I need help”", "Help: WhatsApp, Call support, Common questions (appear once the numbers/links are set)",
   "Legal: Privacy Policy, Terms (appear once the pages exist)", "Your account: <b>Log out</b> · <b>Delete my account</b> (red, asks to confirm)", "App version"],
  ["Guest: “You are exploring without login” + Log in", "Delete needs a recent login: “please log out, log in again, then delete”"],
  ["Log out and delete both remove this phone’s tasks, offers, chats and photos (shared family phones)"],
  [("account_guest_360x800","Guest"), ("SEC-05_delete_confirm_after","Delete confirmation (approved design)")]))
P += S

# ---------------- states ----------------
P.append('<section class="page"><h2>12. States: empty, error, offline, guest</h2><div class="grid">'+
  phone("my_tasks_empty_360x800","Empty — My tasks",150)+phone("otp_wrong_360x800","Error — wrong OTP",150)+phone("account_guest_360x800","Guest — Account",150)+phone("chats_list_360x800","List",150)+
  '</div><table class="t"><tr><th>State</th><th>Rule</th></tr>'
  '<tr><td>Loading</td><td>Grey skeleton cards that look like the content (no full-screen spinners); a spinner only inside the button being pressed.</td></tr>'
  '<tr><td>Empty</td><td>Icon, one plain sentence, and one action (e.g. Post a task / See jobs near you).</td></tr>'
  '<tr><td>Error</td><td>Says what happened and what to do next, next to the place it happened. Input is kept.</td></tr>'
  '<tr><td>Offline / weak network</td><td>Last loaded data stays visible; new posts, offers and messages are saved on the phone and sent later.</td></tr>'
  '<tr><td>Guest</td><td>Can browse; posting a task or sending an offer opens “Log in to continue” (mobile number or Google, “Not now”).</td></tr>'
  '<tr><td>Test mode</td><td>Before Firebase is set up: a yellow note on login screens and in Account. Never shown once real login is on.</td></tr></table></section>')

# ---------------- responsive ----------------
def trio(name, label):
    return f'<div class="trio"><h4>{label}</h4>'+phone(f"{name}_320x568","Small phone 320×568",205)+phone(f"{name}_360x800_font200","Largest font (200 %)",205)+'</div>'
NOTE13 = '<p class="note">Rules: nothing has a fixed height that holds text; screens scroll; rows wrap or scroll sideways; content stays clear of the status bar, notch and navigation bar; the keyboard never hides the field being typed.</p>'
P.append('<section class="page"><h2>13. Small phones &amp; large font (1/2)</h2><div class="trios">'+trio("welcome","Welcome")+trio("otp_wrong","OTP")+'</div>'+NOTE13+'</section>')
P.append('<section class="page"><h2>13. Small phones &amp; large font (2/2)</h2><div class="trios">'+trio("my_tasks","My tasks")+trio("helper_home","Helper Home")+'</div>'+NOTE13+'</section>')
P.append('<section class="page"><h2>13. Tablets &amp; foldables</h2><div class="grid">'+phone("welcome_tablet800x1280","Welcome",300)+phone("task_category_tablet800x1280","Category (5 columns)",300)+phone("account_tablet800x1280","Account",300)+
  '</div><p class="note">On tablets, unfolded foldables and landscape, content keeps a readable maximum width of 640 dp and is centred instead of stretching buttons across the screen. Android 16 ignores portrait locks on large screens, so the app does not rely on one.</p></section>')

# ---------------- hindi ----------------
P.append('<section class="page"><h2>14. Hindi (draft, for review)</h2><div class="grid">'+
  ''.join(phone(f"hi_{n}",c,155) for n,c in [("welcome","स्वागत"),("mobile_login","मोबाइल नंबर"),("customer_home","होम"),("my_tasks","मेरे काम"),("helper_home","कारीगर होम"),("account","खाता")])+
  '</div><p class="note">All 247 texts have a simple-Hindi draft (docs/i18n/HINDI_REVIEW.md). Everyday words stay in English (OTP, SMS, Google, Online, Chat, WhatsApp). The draft is <b>not yet in the app</b> — it goes in after the owner or a native speaker approves it. These screenshots confirm the layouts fit Hindi text.</p></section>')

# ---------------- accessibility ----------------
P.append('''<section class="page"><h2>15. Accessibility</h2><table class="t">
<tr><th>Area</th><th>What the design does</th><th>Status</th></tr>
<tr><td>Contrast</td><td>Main text 16:1, secondary text 4.8:1, buttons 5.5:1</td><td>✓ · two exceptions below</td></tr>
<tr><td>Text size</td><td>Minimum 12 sp; follows the phone’s font size; tested at 200 %</td><td>✓</td></tr>
<tr><td>Touch targets</td><td>Buttons 48–56 dp; list rows at least 56 dp</td><td>✓</td></tr>
<tr><td>Screen readers</td><td>Icon-only buttons have labels (Back, Send, Remove photo); OTP field announced as “6-digit OTP”; decorative images silent</td><td>✓ (check with TalkBack on a phone)</td></tr>
<tr><td>Colour meaning</td><td>Every status badge has words, not only colour</td><td>✓</td></tr>
<tr><td>Dark mode phones</td><td>App stays light; status-bar icons stay dark; OEM force-dark off</td><td>✓</td></tr>
<tr><td>Language</td><td>Plain English now; Hindi draft ready</td><td>⏳ Hindi approval</td></tr>
<tr><td>White text on orange (Post-a-task button)</td><td>2.9:1 — below 4.5:1</td><td>✗ Fix: darker orange <code>#C2410C</code> for the button (5.2:1) or navy text</td></tr>
<tr><td>Grey on light grey (“Cancelled” badge)</td><td>4.35:1 — just below 4.5:1</td><td>✗ Fix: text <code>#4B5563</code> (6.9:1)</td></tr>
</table></section>''')

# ---------------- before/after ----------------
P.append('<section class="page"><h2>16. Before → after (from the audit)</h2><div class="ba">'+
  ''.join(f'<div class="pair">{phone("before_"+b,"Before",112)}{phone(a+"_360x800","After",112)}<p>{t}</p></div>' for b,a,t in [
    ("sign_in","welcome","Black, unreadable sign-in with passwords → simple welcome"),("mobile_login","mobile_login","Unreadable → clean +91 field and SIM picker"),
    ("otp","otp","Six separate boxes (no paste) → one OTP input"),("task_category","task_category","Wrong step names, clipped tiles → fixed")])+
  '</div></section>')

# ---------------- open items ----------------
P.append('''<section class="page"><h2>17. Open design items</h2><table class="t">
<tr><th>Item</th><th>Why it matters</th><th>Needs</th></tr>
<tr><td>Contrast fixes (orange button, Cancelled badge)</td><td>Readability in sunlight</td><td>OK to change colours</td></tr>
<tr><td>Hindi in the app</td><td>Hindi-first users (Ramesh ji)</td><td>Approval of the Hindi draft</td></tr>
<tr><td>Task photos visible to helpers</td><td>Helpers quote better with photos</td><td>Firebase Storage (next technical step)</td></tr>
<tr><td>Notifications (“You got a new offer”)</td><td>Users otherwise must open the app to check</td><td>Firebase Cloud Messaging + small server function</td></tr>
<tr><td>Real location / distance</td><td>“Jobs near you” is not sorted by distance yet</td><td>Decision D7 (location / maps)</td></tr>
<tr><td>Payments (UPI first)</td><td>If the business takes money in the app</td><td>Decision D6</td></tr>
<tr><td>Help, policy and deletion pages</td><td>Play Store requirement; trust</td><td>Your WhatsApp/phone number and page links (D10)</td></tr>
<tr><td>Ratings &amp; reviews, helper verification</td><td>Trust between strangers</td><td>Future design batch</td></tr>
<tr><td>Dark theme</td><td>Optional; the app is deliberately light today</td><td>Future</td></tr>
</table><p class="note">Source files: app screens in <code>app/src/main/java/com/example/ui/</code> · screenshots regenerate with <code>./gradlew recordRoborazziDebug</code> · this document: <code>docs/design/ui-ux/build.py</code>.</p></section>''')

CSS = '''@page{size:A4 landscape;margin:0}
*{box-sizing:border-box}html,body{margin:0}
body{font-family:Inter,"FreeSans",sans-serif;color:#0B1F44;font-size:14px;line-height:1.5;-webkit-print-color-adjust:exact;print-color-adjust:exact}
.page{width:297mm;height:210mm;padding:12mm 14mm 10mm;page-break-after:always;overflow:hidden;position:relative}
.page:after{content:"FixoraX · UI/UX Design";position:absolute;bottom:5mm;right:14mm;font-size:9px;color:#9CA3AF}
h1{font-size:30px;margin:6px 0}h2{font-size:26px;margin:0 0 10px;color:#0B1F44;border-bottom:3px solid #0B57FF;display:inline-block;padding-bottom:3px}
h3{font-size:16px;margin:12px 0 6px;color:#0B57FF}h4{font-size:12.5px;margin:10px 0 4px;text-transform:uppercase;letter-spacing:.04em;color:#6B7280}
ul{margin:4px 0;padding-left:18px}li{margin:2px 0}
.cover{background:linear-gradient(160deg,#fff 55%,#EEF4FF);text-align:center;padding-top:10mm}
.cover .logo{width:70px}.brand{font-size:44px;font-weight:800;font-style:italic;color:#0B57FF;margin-top:-6px}.brand span{color:#FF6B00;font-size:52px}
.sub{color:#6B7280;margin:2px}.meta{color:#6B7280;font-size:11px;margin-top:8px}
.covergrid{display:flex;gap:14px;justify-content:center;margin-top:12px}
.ph{margin:0;display:inline-block;vertical-align:top}.ph img{width:100%;border:1px solid #E5E7EB;border-radius:14px;box-shadow:0 2px 8px rgba(11,31,68,.08);display:block}
.ph figcaption{font-size:10.5px;color:#6B7280;text-align:center;margin-top:4px}
.toc{columns:2;font-size:17px;line-height:2.1}
.cols2{display:grid;grid-template-columns:1fr 1fr;gap:22px}
.t{border-collapse:collapse;width:100%;font-size:12.5px}.t th{background:#EEF4FF;text-align:left;padding:5px 7px;color:#0B1F44}.t td{border-bottom:1px solid #E5E7EB;padding:5px 7px;vertical-align:top}
.cards{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.card{border:1px solid #E5E7EB;border-radius:14px;padding:14px 16px;background:#fff;font-size:14px}.card h3{margin-top:0}
.swatches{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.sw{border:1px solid #E5E7EB;border-radius:12px;padding:8px;display:flex;flex-direction:column;gap:2px;font-size:12.5px}
.sw .chip{height:52px;border-radius:8px;margin-bottom:4px}.sw code{color:#6B7280}.sw em{font-style:normal;color:#0B57FF}
.badges{display:grid;grid-template-columns:auto 1fr auto 1fr;gap:8px 10px;align-items:center}
.bd{display:inline-block;font-weight:700;font-size:12px;padding:3px 9px;border-radius:8px}
.note{color:#6B7280;font-size:12.5px;margin-top:10px}
.comp{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.cp{border:1px solid #E5E7EB;border-radius:12px;padding:10px}.cp h4{margin-top:0}.cp p{font-size:12px;color:#374151;margin:6px 0 0}
.btn{border-radius:10px;padding:11px;text-align:center;font-weight:600;font-size:13px}.btn.p{background:#0B57FF;color:#fff}.btn.o{border:1px solid #E5E7EB}.btn.t{color:#6B7280}
.g{color:#4285F4;font-weight:800}.field{border:1px solid #E5E7EB;border-radius:10px;padding:10px;font-size:14px}
.otp{display:flex;gap:5px}.otp i{flex:1;height:34px;border:1px solid #E5E7EB;border-radius:6px;font-style:normal;display:flex;align-items:center;justify-content:center;font-weight:600}.otp i.f{border-color:#0B57FF}.otp i.n{border:2px solid #0B57FF}
.fc{display:inline-block;border:1px solid #E5E7EB;border-radius:8px;padding:4px 8px;margin:2px;font-size:11px}.fc.on{background:#E8DEF8;border-color:#E8DEF8}
.av{width:40px;height:40px;border-radius:50%;background:#FFE7D6;color:#FF6B00;font-weight:700;font-size:18px;display:flex;align-items:center;justify-content:center}
.nav{display:flex;justify-content:space-around;border-top:1px solid #E5E7EB;padding-top:6px;font-size:10px;text-align:center;color:#6B7280}.nav .on{color:#0B57FF;font-weight:700}
.map{width:100%;height:auto;margin-top:6px}
.flow td:first-child{width:150px}.flow td:last-child{width:70px;text-align:center}
.screen .row{display:flex;gap:22px}.screen .phones{display:flex;gap:12px;flex:none}.screen .desc{flex:1;font-size:13.5px}
.purpose{font-size:16px;color:#0B1F44;margin:0 0 6px;font-weight:500}
.divider{background:#0B57FF;color:#fff;display:flex;flex-direction:column;justify-content:center;padding-left:24mm}
.divider h2{color:#fff;border-color:#FF6B00;font-size:32px}.divider p{font-size:16px;opacity:.9}.divider:after{color:#cfe1ff}
.grid{display:flex;gap:14px;flex-wrap:wrap}
.trios{display:grid;grid-template-columns:1fr 1fr;gap:24px}.trio{display:flex;flex-wrap:wrap;gap:6px}.trio h4{width:100%;margin:0}
.ba{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.pair{display:flex;flex-wrap:wrap;gap:6px}.pair p{width:100%;font-size:11px;margin:0}
'''
doc = f'<!doctype html><html lang="en"><head><meta charset="utf-8"><title>FixoraX UI/UX Design</title><style>{CSS}</style></head><body>{"".join(P)}</body></html>'
open('FixoraX_UI_UX_Design.html','w').write(doc)
print(len(P),'pages')
