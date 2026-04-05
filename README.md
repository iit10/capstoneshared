# SnapCal Prototype

This repository contains the latest SnapCal prototype in plain HTML.

## Quick Start

1. Clone the repository:
   ```bash
   git clone https://github.com/iit10/capstoneshared.git
   cd capstoneshared
   ```
2. Open `snapcal_working.html` in a browser.
3. You can also open `snapcal_preview.html` (it is synced to the same latest app).

## Recommended (Local Server)

Using a local server avoids browser restrictions and gives the best behavior.

```bash
python -m http.server 5500
```

Then open:

- `http://localhost:5500/snapcal_working.html`
- or `http://localhost:5500/snapcal_preview.html`

## Current Features

- Local sign up/login flow (prototype auth)
- Meal planning and shopping list generation
- Manual and online recipe import
- Custom recipe create/edit/delete
- Dashboard activity logging, calorie goals, and workout calendar
- Progress analytics comparing consumed vs burned calories

## Notes

- User/app data is stored in browser localStorage.
- Online recipe search requires internet access.
