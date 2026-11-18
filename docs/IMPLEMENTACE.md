# Implementační plán – Novation Impulse pro Bitwig

Pracovní seznam funkcionalit, které plánujeme do pluginu implementovat.
Doplňková dokumentace: [HARDWARE.md](HARDWARE.md) (hardware a SysEx),
[CONTROLS.md](CONTROLS.md) (mapování ovladačů), [README](../README.md) (instalace a build).

**Stav:** verze 1.3, branch `improvements`, změny committed 17.11.2026
(identity `Binaryca <139915326+Binaryca@users.noreply.github.com>`).

## Hotovo (v1.2)

- [x] Migrace na Bitwig Extension API 25 (Bitwig 6.x), oprava pádu při inicializaci
- [x] Track bank 8 stop × 2 sends × 1 scéna (jedna scéna = správné LED padů)
- [x] Clip launch pady s LED stavy + Shift + Launch = stop všech clipů
- [x] Displej: text 8 znaků (doplnění mezerami), číselná plocha 3 znaky (`67 09`), `Scene N`
- [x] Volitelné perzistentní popisky ovladačů (`67 05`) – preference **Control labels**
- [x] Dokumentace: `CONTROLS.md`, `HARDWARE.md`, `README.md`
- [x] Build helpery pro Windows (`build.cmd` / `build.ps1`), JUnit testy (8)

## Priorita 0 – rychlé úpravy (návrh pro vydání 1.3)

- [x] **LED transportu podle stavu Bitwigu** *(v1.3; pozor: LED zpětná vazba
  transportu není v dokumentaci hardware potvrzená – ověřit na hardware)*
  Tlačítka Play / Record / Loop rozsvěcovat podle `transport.isPlaying()`,
  `isArrangerRecordEnabled()` a `isArrangerLoopEnabled()`. Ověřit hodnoty LED na
  hardware (0 = off, jiná hodnota = on).
  *Soubory:* `observers/TransportLedObserver`, `MidiSend`.

- [x] **Automatické odeslání šablony při startu** *(v1.3, default ON)*
  Nová preference *Send template at startup* (on/off, default on) – ušetří
  klikání v nastavení po každém restartu Bitwigu.
  *Soubory:* `Preferences`, `NovationImpulseExtension`.

- [x] **Undo / Redo** *(v1.3)*
  Přidat shift varianty transport tlačítek: Shift + Rewind = Undo,
  Shift + Fast-Forward = Redo (`application.undo()` / `application.redo()`).
  Zobrazení `Undo` / `Redo` na displeji.

- [x] **Vymazání všech mute / solo** *(v1.3)*
  Shift + tlačítko Mute/Solo = vypnout všechny mute (v solo režimu všechny
  solo). Iterace přes track bank (8 viditelných stop + master), displej
  `ClrMutes` / `ClrSolos`.
  *Soubory:* `commands/mixer/ClearAllMutesSolosCommand`, `Tracks`.

- [x] **Zpráva „Ready“ po inicializaci** *(v1.3)*
  Po odpovědi na device inquiry zobrazit na displeji krátký stav (model +
  ready), aby bylo vidět, že extension proběhl v pořádku.
  *Soubory:* `SysexCallback`, `SysexSend`.

## Priorita 1 – střední rozsah

- [x] **Více sends (Send 3–6) a stránky mixéru** *(v1.3)*
  Rozšířit track bank na 6 sends; tlačítko Mixer cyklicky listuje stránkami
  Pan → Send 1–6, název stránky se zobrazí na displeji.
  *Soubory:* `Midi.SEND_COUNT`, `Tracks`, `EncoderMixerModePage`,
  `EncoderMixerCommand`.

- [x] **Kontrola master faderu na Impulse 61** *(v1.3, ověřeno)*
  Ověřit, že CC 8 ovládá master track; pokud ne, opravit mapování pro 49/61.
  V kódu: `FaderCommand` CC 8 → `Tracks.get(8)` = master track, otestováno na
  hardware. *(Pro Impulse 25 zvlášť `FaderCursorCommand` → cursor track.)*

- [x] **Data knob – přepínač presetů** *(v1.3, PopupBrowser, otestováno 16.11.2026)*
  V normálním stavu knob mění šablonu a MIDI neposílá (není v zónách).
  **Prog Change režim** (Shift + MIDI Chan, exit = Enter) přepne knob na
  posílání **Program Change na kanálu 1**. První implementace přes
  `Device.loadPreset()` **spadla** – staré preset API je od API 25 zcela
  uzamčené (ověřeno dekompilací: `checkCanUseOldPresetAPI` volá i
  `switchToNext/PreviousPreset`); na API 1 by šlo použít, ale to by uzamklo
  zbytek moderního API. BrowsingSession API je **celé deprecated**
  („Use PopupBrowser instead" – dle lokální oficiální dokumentace Bitwigu).
  Finální řešení = **moderní PopupBrowser** (vzor DrivenByMoss `BrowserImpl`):
  `host.createPopupBrowser()`, otevření přes
  `cursorDevice.replaceDeviceInsertionPoint().browse()` (dialog pro výběr
  presetů vybraného zařízení), content typ „Preset" se zajistí přes
  `selectedContentTypeIndex` (browser se otevře na indexu 0 = Devices, kód
  přepne na první tab obsahující „Preset" = *Bitwig Presets*).
  **Pohyb seznamu řídí `browser.selectNextFile()/selectPreviousFile()`**
  (nativní akce browseru). Ověřeno na hardware: hýbou viditelným
  zvýrazněním a **browser je sám zarazí na krajích seznamu** (žádný únik
  fokusu). Alternativy nefungují: API kurzor results sloupce
  (`cursorResult.selectNext`) hýbe jen „skrytým" výběrem, šipky
  (`application.arrowKeyDown/Up`) na krajích seznamu unikají mimo seznam
  (fokus skočí do vyhledávání/filtrů) a mimo browser hýbají stopami,
  `cursorResult.hasNext/hasPrevious` jsou v novém browseru mrtvé (vždy
  false), `bank.cursorIndex()` čte vždy -1 (ale `bank.itemCount()` a
  `isSelected()` sken položek jsou živé). Žádná seed šipka (není potřeba,
  odstranila drift +1). Otevírání: otočení knobu armuje **lepkavý
  `pendingOpen`** → ticker volá `browse()` max 1× za cyklus (`browseSentAt`),
  potvrzení dělá **exists() observer** (při open=true maže požadavek, při
  false resetuje cyklus); retry jen do timeoutu 1,5 s, max 3 pokusy
  (dvojité `browse()` během otevírání způsobovalo blik open/close/open –
  zachyceno v logu jako dvě `open` 267 ms od sebe). Kroků šetřeno
  `closing` příznakem (commit/cancel zavírá asynchronně, šipky v okně
  zavírání by sletěly do hlavního okna a hýbaly stopami). Delta z PC čísel
  (s wrapem, testy) se hromadí (±64) a vypouští 1 krok/tick (2 při backlogu
  ≥ 6); první PC po startu jen „naučí" pozici (sentinela -1 dala k+1 kroků).
  Nahraní přes `browser.commit()` **1,2 s** po zastavení knobu (kratší
  doba poblikávala při pomalém otáčení). Browser se otevírá přímo na
  nahraném presetu → žádný drift mezi sessions. **Audition (náhled) je
  během knob-scanu vypnutý** (každý krok jinak nahraje preset pro náhled →
  růst heapu Bitwig JVM → jednou OOM crash systému, error 1455 pagefile).
  Displej: název presetu event-driven přes `cursorResult.name()` observer
  (API hodnota zaostává 1–2 kroky, update hned po krocích ukazoval
  „předchozí" název). Ticker (50 ms) řídí open/move/commit, protože
  `scheduleTask` z MIDI callbacku se nikdy nevykoná. Otestováno kompletně
  16.11.2026: rychlé/pomalé otáčení, kraje seznamu, opakované první
  otočení bez bliku, paměť stabilní.
  *Soubory:* `Midi.CH1_PC`, `commands/DataKnobCommand`, `Tracks.getCursorDevice()`,
  test wrap logiky.

- [x] **Transport scrub** *(v1.4, implementováno 17.11.2026)*
  Rewind/Forward v mixer režimu (mix panel zavřený) přetáčí transport:
  klik = 1 beat; **držení = souvislý scrub (~7 beatů/s)** generovaný
  tickerem (50 ms, krok každé 3 tiknutí) — hardware při držení **neposílá
  nic** (jen 127 press / 0 release; dřívější „opakování" v logu byly
  releasy kliků). Každý krok = `transport.setPosition(max(0,
  getPosition().get() ± 1.0 beat))` — absolutní beat-time, bytecode
  **clean**. Poučky:
  **`transport.rewind()` = skok na začátek aranžmá** („Rewinds to the
  beginning of the arrangement" — ne scrub!), `fastForward()` jen krok;
  **`incPosition(beats, snap)` interpretuje amount „relative to the given
  range"** (bez parametru range = normalizace přes délku aranžmá → skok na
  začátek/konec) → zamítnuto, scrub počítáme ručně. Další poučka:
  **`.get()` na nepřihlášené value hodí výjimku** („Either call
  markInterested() or add at least one observer") → `getPosition()`
  `markInterested()` v konstruktoru. Diagnostika odstraněna po ověření
  17.11.2026.
  Protokol režimových tlačítek konbů (důležité, ať to nikdo „neopraví"):
  tlačítka posílají **jen hodnotu 0** (stav, ne velocity — na rozdíl od
  transportu) → mode commandy mají match na jakoukoliv hodnotu s
  idempotentním handle (odemčeno 17.11 večer; `Midi.isPressed` na nich
  nikdy nesedělo, proto se režim dřív nepřepnul a na displeji nebylo
  `Mixer`): stisk Plugin+MIDI současně = `B1:09` → režim MIXER + displej
  `Mixer`; Plugin samo = `B1:10` → PLUGIN; MIDI samo = `B1:08`
  (nepoužíváme).
  *Soubory:* `commands/transport/TransportRewindCommand`,
  `TransportForwardCommand`.

- [x] **Metronom a uložení projektu** *(v1.4, implementováno 17.11.2026)*
  Shift + Play = metronom, displej `Click On` / `Click Off`;
  Shift + Stop = uložení projektu, displej `Save`. Pozor: `application.save()`
  v API 25 **neexistuje** (ukládání jde jen přes action systém
  `application.getActions()`/`getAction(id)`) a ID akcí se mezi verzemi
  Bitwigu mění (shipped scripty: `getAction("Undo")`, novější:
  `slice_to_multi_sampler_track`) → akce se hledá jednorázovým skenem podle
  názvu (`Save`), ne pevným ID; pokud se nenajde, displej `No Save`. Metronom
  musí jít přes value API `transport.isMetronomeEnabled().toggle()` —
  klasické `toggleClick()`/`addClickObserver()` jsou gated (deprecated od
  API 2, havarie extensionu při startu; bytecode audit proxy potvrdil
  GATED, ostatní transport metody i celé Action API clean).
  *Soubory:* `commands/transport/MetronomeCommand`, `SaveCommand`.

- [x] **Navigace zařízení v plugin režimu** *(v1.4, implementováno 17.11.2026)*
  V plugin režimu: Rewind = `cursorDevice.selectPrevious()`, Forward =
  `selectNext()` (Nektar LX61: Instrument mód). Displej: název zařízení
  (event-driven přes `name()` observer, hodnota zaostává za výběrem).
  Gating: `encoderMode == PLUGIN`; v otevřeném mix panelu mají přednost
  scény (SceneUp/Down registrovány dřív, first-match routing) → transport
  rewind/forward zůstává v mixer režimu s zavřeným mix panelem.
  *Soubory:* `commands/transport/DeviceNavigateCommand`,
  `NovationImpulseExtension` (registrace před transport commands).

- [x] **Fader soft takeover (pickup)** *(v1.4, implementováno 17.11.2026)*
  Fadery nejsou motorizované → po přepnutí banky stop se fyzická poloha
  liší od hodnoty a první pohyb udělal skok. Stavový automat per fader
  (MUTED / LOW / HIGH / ACTIVE) portovaný z Nektar logiky `L04F`: hodnota
  se přijme, jen když je fyzická poloha blízko (okno ±3), nebo když fader
  **přejede hodnotu** (z LOW `incoming > current`, z HIGH `incoming <
  current`; hrany 127/0 řešeny); po pauze 750 ms se vzdálený fader znovu
  odpojí. Reset při posunu banky stop: synchronně v
  `TrackBankUp/DownCommand` (před dalšími fader zprávami) + scrollPosition
  observer (scrollování z UI) a při změně cursor track (Impulse 25).
  Odmítnutý pohyb: displej ukáže název stopy + **skutečnou hodnotu**
  (uživatel ví, kam dojet).
  **Cross-talk gate** (hardware zvláštnost Impulse): když uživatel hýbe
  jedním faderem, hardware občas sám vyšle pozici jiného, v klidu
  stojícího faderu (±2 ADC jitter, nejspíš prosakování dotykových
  senzorů) — zprávy dorazí 0–18 ms po zprávě pohybovaného faderu a u
  faderu odpočívajícího na své hodnotě vypadají jako nové uchopení →
  hlasitost „wiggluje". Gate: zpráva blízko hodnoty (±4), která dorazí
  do 150 ms po zprávě **jiného** faderu, se tiše zahodí (decision
  `IGNORE` — žádný display, žádná změna stavu, nebere se jako „stream");
  vzdálené pohyby a vlastní zprávy streamujícího faderu projdou.
  Konkvěnda: uchopení (catch) druhého faderu se při současném pohybu
  dvou faderů odkládá, dokud druhý na ~150 ms neztiší.
  Poučky: `markInterested()` volat **přes bank item proxy při initu**
  (Tracks.get(i) vrací null pro prázdné sloty — markovat přes existující
  stopy nestačí!); skála `value()` je 0..1, CC srovnání =
  `round(value × 128)` dle `set(a, 128)`; externí změny hodnoty se
  korigují samy (stav se počítá vůči aktuální hodnotě v každé zprávě) →
  žádný observer-reset na volume; **hardware může sám vysílat zprávy
  faderů, které nikdo nedrží** — diagnostika `Fader[i]: in=.. cur=..
  ACCEPT/reject/ignore` (debug-gated) ukázala interleaved zprávy jiných
  faderů ve stejných milisekundách; čas (System::nanoTime injektovaný
  LongSupplier) je jediný spolehlivý diskriminátor.
  Konstanty: `WINDOW=3`, `CROSS_TALK_WINDOW=4`, `OTHER_STREAM_NANOS=150ms`,
  `TICK_MS=250`, `TIMER_TICKS=3`. Encodéry takeover nepotřebují
  (endless = relativní). 14 unit testů (automat + cross-talk gate).
  *Soubory:* `FaderSoftTakeover`, `FaderSoftTakeoverTest`, `FaderCommand`,
  `FaderCursorCommand`, `TrackBankIndexObserver`, `TrackBankUpCommand`,
  `TrackBankDownCommand`, `NovationImpulseExtension`.

- [x] **Device on/off v plugin režimu** *(v1.5, implementováno a otestováno
  18.11.2026)*
  Tlačítka pod fadery (CC 9–16) v plugin režimu přepínají enabled state
  prvních 8 zařízení chainu, **který obsahuje vybrané zařízení** (banka =
  `cursorDevice.createSiblingsDeviceBank(8)` — sleduje úroveň výběru:
  vnořený drum machine lane → zařízení lane, top-level → top chain; první
  návrh `cursorTrack.createDeviceBank(8)` = top chain selhal na vnořených
  projektech — Drum Machine = jediný top device, zbytek Empty), toggle =
  `isEnabled().toggle()`, protože `toggleEnabledState()` je deprecated od
  API 2 a v API 25 hází!), Shift +
  tlačítko vybere zařízení (`cursorDevice.selectDevice(device)`; Nektar
  LX61: XC_INST_K4TT0–7). Master button (CC 17) v plugin režimu no-op
  (pohlcen příkazem — nemůže překvapivě mutovat master). Prázdný slot →
  displej `Empty`. LED tlačítek: v plugin režimu = enabled state
  (`DeviceOnOffObserver`), mute/solo callbacky dostaly gate
  `encoderMode != PLUGIN`, refresh LED při přepnutí encoder režimu
  (obě směry, `RefreshAllLightsCallback` má větev pro plugin). Displej:
  název zařízení (announce pattern — select = observer na
  `cursorDevice.name()`, toggle = observer na `isEnabled()` bank itemu).
  Poučky: **javap ukazuje jen deklarované metody, ne zděděné** — první
  průzkum tvrdil, že Track nemá device chain banku; fakt: `Track extends
  Channel extends DeviceChain` → `createDeviceBank` zděděno (top chain!
  bez nesting caveatu siblings banky); **deprecated metody z API 2 za
  běhu v API 25 VYHODÍ výjimku** — `toggleEnabledState()` existuje v
  rozhraní, ale při prvním stisku hodil „deprecated since API version 2:
  Use isEnabled().toggle()" (stejná past jako `toggleClick()` u
  metronomu); javap bez `-v` neukazuje `@Deprecated` → deklarované metody
  vždy ověřovat proti hodnotovému API; `ObjectProxy.exists()` na prázdné
  sloty; markInterested přes bank proxy při initu (`name`, `isEnabled`,
  `exists`). 3 unit testy (`deviceIndex`).
  *Soubory:* `Tracks`, `commands/devices/DeviceOnOffCommand`,
  `DeviceOnOffCommandTest`, `observers/DeviceOnOffObserver`, `MuteCallback`,
  `SoloCallback`, `RefreshAllLightsCallback`, `EncoderPluginModeCommand`,
  `EncoderMixerModeCommand`, `NovationImpulseExtension`.

## Priorita 2 – experimenty a větší změny

- [ ] **Režim C s popiskami na displeji**
  Hardware umí zobrazovat popisky a hodnoty automaticky (Mode C). Problém:
  pady musí zůstat v Mode B (clip launch). Řešení dle HARDWARE.md: fadery/konby
  Mode C přes hlavní port + pady Mode B přes control port (`MIDIOUT2/3`).
  Vyžaduje: deklarovat další MIDI out porty v `NovationImpulseExtensionDefinition`,
  testovat na hardware, připravit fallback (revert na Mode B).

- [ ] **Expression pedal / mod wheel – cílová volba**
  Preference pro cíl CC 11 (aktuálně jde do note input). Zvážit mapování na
  vybraný parametr; nejdřív zjistit, co Bitwig API nabízí (cursor controls).

- [ ] **Plná podpora Impulse 25 a 49**
  Projít mapování pro menší modely (1 fader, 0 tlačítek pod fadery), testy
  proti `ImpulseModel`.

- [ ] **Punch in / out (loop range)** *(návrh – Nektar LX61)*
  Shift varianty transportu pro nastavení začátku/konce loopu
  (`transport.getPosition().set(...)`, in/out dle Nektaru LX61). U nás jsou
  Shift+Loop/Rewind/Forward volné v mix režimu (scény jsou bez Shiftu).
  *Soubory:* `commands/transport/`, `Transport`.

- [ ] **Master ↔ cursor track fader** *(návrh – Nektar LX61)*
  Tlačítko Master přepíná, zda fader 9 řídí master track nebo vybranou stopu.
  *Soubory:* `FaderCommand`, `Midi.BUTTON_MASTER`.

- [ ] **Data knob – drobná vylepšení** *(poznámky z implementace 16.11.2026)*
  a) Preference *Preview při otáčení* – náhledy (audition) jsou teď natvrdo
  vypnuté kvůli paměti (OOM crash); volitelný přepínač, default off.
  b) Plug-in Presets tab pro VST zařízení – teď se vždy volá první tab
  obsahující „Preset" (= *Bitwig Presets*); u pluginů by byl správný
  *Plug-in Presets* (rozpoznat podle typu zařízení).
  c) Experiment: stisk data knobu v PC režimu jako commit – zjistit, zda
  press něco vysílá; „otoč → stiskni = nahrát" by bylo přesnější UX
  než časovač 1,2 s.

## Údržba a infrastruktura

- [x] **Git commity** – rozděleny do logických commitů (build/CI, dokumentace,
  rework na API 25, P0 funkce, data knob + wiring) 17.11.2026.
  Push čeká na fork `tlipinski/bitwig-novation-impulse` pod účtem `Binaryca`
  (origin ukazuje na původní repozitář).
- [ ] **CI** – GitHub Actions workflow (JDK 21 + Maven, artefakt `.bwextension`),
  případně nahradit `circle.yml`.
- [x] **Release 1.4** *(17.11.2026)* – tag `v1.4` na `myfork/improvements`,
  changelog v README (Changes in 1.4), release + upload `.bwextension`
  vytvořeny přes GitHub API (uložený push token).
- [x] **Release 1.5** *(18.11.2026)* – tag `v1.5` na `myfork/improvements`,
  changelog v README (Changes in 1.5), release + upload `.bwextension`
  vytvořeny přes GitHub API.
- [x] **Verze** – bump na 1.4 (pom.xml).

## Testovací checklist (hardware)

Po každé implementaci projít. **Otestováno 16.11.2026 na Impulse 61, verze 1.3
(hardware + log bez chyb):**

1. [x] Restart Bitwigu – log bez chyb extensionu.
2. [x] Fader/encoder pohnou displejem: název + číselná hodnota.
3. [x] Preference **Control labels** zapnuta → popisky odeslány; šablona uložena na Impulse.
4. [x] Transport LED reflektují stav přehrávání.
5. [x] Clip launch: pady rozsvěcují podle stavu, Shift + Launch stopne všechny clipy.
6. [x] Scény přes Rewind/Forward/Loop v Mix panelu, `Scene N` na displeji.
7. [x] Stránkování banky (Shift + track tlačítka) zobrazí `[first-last]`.
8. [x] Mute/Solo LED v obou režimech, přepínání tlačítkem Mute/Solo,
   Shift + Mute/Solo = clear all (`ClrMutes` / `ClrSolos`).

## Otevřené otázky

- Mode C: stojí za riziko, nebo zůstat u Mode B + vlastní displej?
