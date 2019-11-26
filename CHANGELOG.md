# Changelog

## 2.0.1 (2019-11-26)

- Fixed bug where you couldn't use gates :S
    - Caused by a last-minute change to mod prefix that wasn't correctly updated in rules.csv
    - Thanks to Sunflare#3581 on Discord for the report!

## 2.0.0 (2019-11-24) (not compatible with previous versions)

- Added final quest
- Added Console Command to spawn a gate
- Added Console Commands to debug the mod
- Polished existing quests and intel. New icon, intel is now marked as completed, better wording, etc
- Readded LazyLib as dependency (to use its Kotlin stdlib)
- Fixed compatibility issue with Vayra's Sector where a gate-related event in VS was not shown
- Hopefully fixed bug where the dialog did not appear after jumping in the first quest
- Way more refactoring than was necessary

## 1.3.1 (2019-10-9)

- Fix for crash when completing a question after the intel has timed out
    - Thanks to Liork on Discord for the report
- Known issue: intel can time out, apparently :(

## 1.3.0 (2019-10-02)

- Gates now get their original name back after being deactivated
- Slightly improved gate intel formatting
- _Balance is for gymnasts._ Added configuration for # of activation codes

## 1.2.0 (2019-09-29)

- Gates may now be deactivated to reclaim an activation code
- Number of reward activation codes increased to 3 from 2
- Now prompts player to enable transponder when needed
- Added hyperspace transition animation
- Gates Awakened quests are now marked with the Story intel tag

## 1.1.1 (2019-09-25)
- Fixed crash that could occur when updating gate intel 
    - Thanks to Sunflare on Discord for the report

## 1.1.0 (2019-09-24)

- Added new intel that displays all active gates
- Updated intel icons to be cooler
- Reduced fuel requirement to use gates to 80%
- Removed dependency on LazyLib
- Fixed bug where the second quest would not appear, even if you met the requirements

## 1.0.1 (2019-09-12)

- Fixed jump using double the stated fuel price
- Fixed typo in the second mission opening

## 1.0.0 (2019-09-12)

- Initial Release
