
# Collection Log

A Minecraft plugin for tracking collected items with a clean, intuitive GUI.

## Features

- **Comprehensive Item Tracking**: Track any item in the game, including those with custom model data.
- **Intuitive GUI**: Beautiful and responsive collection log interface accessible with `/log`.
- **Customizable Categories**: Organize items into categories for easy browsing.
- **Visual Feedback**: Get notifications when collecting new items, with optional sound and particle effects.
- **Persistence**: All collection data is saved and loaded automatically.
- **Admin Commands**: Reset data and reload configurations with simple commands.

## Commands

- `/log` - Open the collection log GUI
- `/log category <id>` - Open a specific category directly
- `/log reload` - Reload the plugin configuration (requires permission)
- `/log reset` - Reset your collection data (requires permission)

## Permissions

- `collectionlog.use` - Allows players to use the collection log (default: true)
- `collectionlog.admin` - Allows admin commands for the collection log (default: op)

## Configuration

### config.yml
General plugin settings, including notification preferences and save interval.

### items.yml
Define categories and items for the collection log. Each item can have:
- Material
- Display name
- Custom model data (optional)
- Lore

### gui.yml
Customize the appearance of the collection log GUI, including:
- Inventory size and title
- Border items
- Button placement
- Item appearance for collected/uncollected items

## Development

Built with Paper API 1.20.6 and utilizing the following design principles:
- Clean, modular code structure
- Efficient data management
- Responsive user interface
- Comprehensive error handling

## License

This project is licensed under the MIT License - see the LICENSE file for details.
