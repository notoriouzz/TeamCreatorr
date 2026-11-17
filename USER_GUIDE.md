# TeamCreatorr – User guide

TeamCreatorr has a floating menu at the bottom with five places: **Home · Players · [+ New match] · Standings · Matches**. Settings are behind the gear icon on the Home screen. Detail screens and the new-match steps have a back arrow at the top left instead of the menu.

## Players

- Tap **+ Player** to add one player (name and optional nickname) or several at once (one name per line).
- Names always start with a capital letter: "daan" is saved as "Daan".
- Every name must be unique, so that everyone is easy to find in the teams and the standings.
  - If a name already exists, add a letter, for example "Daan B.".
  - If the name belongs to an **inactive** player, the app asks whether it is the same person. If it is, that player becomes active again with their full history. If not, you choose a different name.
  - When you add several players at once, all new names are saved straight away. Names that already exist then come up one by one ("Name 1 of 2"), and you can rename or skip each of them.
- Search, and sort by **Rating**, **Points** or **Name**. Inactive players are in a collapsed block at the bottom.
- Tap a player to open their card. The pencil changes the name, nickname or photo. The photo always stays on your phone.
- **Long names.** Where space is short (standings, top 3, teams and the share images) one rule applies: if the name fits, you see the name. If not and the player has a nickname, you see the nickname. Otherwise only that name gets a bit smaller (other names keep their size), and if it still does not fit it is shortened with "…". The player list and the player card always show the full name.
- Under **⋮** you can:
  - **Set inactive.** The player no longer appears in the pickers, but their history and points stay. You can make them active again at any time.
  - **Delete permanently.** Name, nickname and photo are erased and the player disappears from all lists and the standings. Old matches stay correct: there the player is shown as "Former player" and their goals count as "Unknown". This cannot be undone. A player who never played is removed completely.

## New match

Tap the big **+** in the menu.

1. **Setup:** choose the team size (3 v 3 up to 11 v 11), the date, and whether the app should split fairly by rating (on by default).
2. **Players:** pick exactly the number of players needed. "Everyone" and "Clear" help.
3. **Duos** (optional): see below.
4. **Teams:** the app makes two teams. The strength bar shows the total rating of each team and whether it is fair.
   - Drag a player onto a player in the other team to swap them.
   - To scroll, swipe next to the players. While dragging, the screen scrolls by itself when you reach the top or bottom edge.
   - **Again** makes a new split. **Confirm** saves the match.

After **Confirm** you can **Share** the line-up as an image, or tap **Continue**. You then see the match with two buttons: **Enter result** and **Enter later**. Enter later takes you back to Home.

## Duos

A duo is two players who always play **together**, or always **apart** (against each other).

- Add a duo in step 3: first choose Together or Apart, then pick two players from the ones who play. The same player cannot be picked twice, and players who are already in another duo are greyed out.
- The switch turns a duo on or off for this match.
- To change Together/Apart, delete the duo and make it again.

## Results and goal scorers

Open the match and tap **Enter result**. Matches that have been played but have no result are listed under **Action needed**.

- Set the score per team with **−** and **+**.
- With **Track goal scorers** on, tap a player to give them a goal, and tap their green number to take one away. Goals you don't assign are shown as **Unknown**. You can't give more goals than the team scored. If you tap − and the score drops below the goals already assigned, all scorers of that team are cleared (they become Unknown); tap **Undo** in the notice to bring back the score and the scorers.
- With tracking off, only the final score is saved.
- The app remembers your last choice.

After saving you return to the match. A notice says "Result saved · standings updated" and has a **Share** button. At the bottom you can **Edit result** or tap **View standings** to see the new standings.

On the match screen, scorers are shown as "Daan ×2". Unassigned goals are combined into one line, "Unknown ×N". If no goal is assigned to anyone, the screen just says "Goal scorers not tracked".

## Standings and points

For every match played, a player gets:

| Part | Default |
|---|---|
| Attendance | 1 point |
| Draw | 1 point |
| Win | 2 points |

You can change these numbers in **Settings → Point system**. After you confirm, everything is recalculated, including earlier matches.

The standings table shows **P** (played), **W** (won), **D** (drawn), **L** (lost), the **ball** (goals) and **PTS** (points). Under each name, five small blocks show the **form** of the last five matches (green win, grey draw, red loss; newest on the right). In Dutch the columns are P · W · G · V, in Turkish O · G · B · M. Tap **Share** to send the standings as an image with the same columns (top 12, without form or photos).

In a player's profile, **Points breakdown** shows how their points add up: attendance, draws and wins (always shown, also when 0), corrections, and the points per match. You can add a **correction** there, with a number of points (also negative) and a reason, for example for organising a tournament.

## Rating

Every player gets a rating from **55 to 95**, like in FC. Your rating compares you with the rest of the group: the best player ends up around 90–95, the middle around 75, and the weakest around 55–60.

**Step 1 – performance (0–1)**, from four parts:

| Attribute | Weight | How |
|---|---|---|
| **WIN** | 35% | (wins + ½ × draws) ÷ matches played |
| **FORM** | 25% | last 5 matches, win = 1, draw = ½, loss = 0; the newest match counts most (5-4-3-2-1) |
| **GOALS** | 20% | goals per match ÷ the highest average in the group; only matches where at least one goal was assigned to a player count |
| **DEFENCE** | 20% | 1 − (goals conceded by your team per match ÷ the highest average in the group) |

If there is no goal information, the GOALS part is left out and the other weights grow in proportion.

**Step 2 – compare with the group.** Players are ranked by performance (worst 0, best 1).

**Step 3 – FC scale.** `basis = 55 + 40 × (0.7 × rank + 0.3 × performance)`.

**Step 4 – reliability.** New players start at the group average and move towards their own level as they play more: halfway after 3 matches, 80% of the way after 12 matches.

The card shows the four attributes as numbers from 0 to 99. The colour is **gold** from 75, **silver** from 65 to 74, and **bronze** below 65. Manual point corrections and matches without a result never affect the rating.

## Settings

- **Group name:** shown at the top of the shared images.
- **Theme:** System, Light or Dark. **Language:** System (default), English, Dutch or Turkish. With System the app follows your phone's language when it is Dutch, English or Turkish, and uses English for any other language. On Android 13 and newer you can also pick the language in the Android settings for the app.
- **Notices:** turns the short message at the bottom after an action on or off.
- **Point system:** see above.
- **Data:** make a backup, restore a backup (merge or replace), or start over.

## Backup and privacy

The app works without internet and collects nothing. Everything stays on your phone, so make a backup now and then (**Settings → Make backup**) and keep the file in a safe place. Profile photos are never included. With **Restore backup** you can merge the file with what is in the app, or replace everything.

**Moving to a new phone:** make a backup on the old phone, install TeamCreatorr on the new phone, and restore the backup there.
