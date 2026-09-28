import sys
from PIL import Image, ImageEnhance

QUAD_CHARS = {
    0b0000: ' ',     0b0001: '▗',    0b0010: '▖',
    0b0011: '▄',     0b0100: '▝',    0b0101: '▐',
    0b0110: '▘',     0b0111: '▟',    0b1001: '▚',
    0b1010: '▌',     0b1011: '▙',    0b1100: '▀',
    0b1101: '▞',     0b1110: '▛',    0b1111: '█',
}

def convert(image_path, output_path, width=180):
    img = Image.open(image_path).convert('RGB')
    img = ImageEnhance.Brightness(img).enhance(1.15)

    ratio = (img.height / 2) / img.width * (width / 2)
    height = max(int(ratio), 1)
    img = img.resize((width, height * 2), Image.LANCZOS)
    px = img.load()

    def bright(r, g, b):
        return int(r * 0.299 + g * 0.587 + b * 0.114)

    def avg(colors):
        n = len(colors)
        if n == 0:
            return (0, 0, 0)
        return tuple(sum(c) // n for c in zip(*colors))

    lines = []
    for y in range(0, height * 2, 2):
        row = []
        for x in range(width):
            tl = px[x, y]
            tr = px[min(x + 1, width - 1), y]
            bl = px[x, y + 1]
            br = px[min(x + 1, width - 1), y + 1]

            bvals = [bright(*c) for c in (tl, tr, bl, br)]
            thresh = sum(bvals) / 4

            pattern = (
                (1 if bvals[0] >= thresh else 0) << 3 |
                (1 if bvals[1] >= thresh else 0) << 2 |
                (1 if bvals[2] >= thresh else 0) << 1 |
                (1 if bvals[3] >= thresh else 0)
            )

            swap = False
            if pattern == 0b1000:
                pattern = 0b0111
                swap = True

            ch = QUAD_CHARS.get(pattern, ' ')

            bright_colors = [c for c, b in zip((tl, tr, bl, br), bvals) if b >= thresh]
            dark_colors = [c for c, b in zip((tl, tr, bl, br), bvals) if b < thresh]

            fg = avg(bright_colors) if bright_colors else avg((tl, tr, bl, br))
            bg = avg(dark_colors) if dark_colors else avg((tl, tr, bl, br))

            if swap:
                fg, bg = bg, fg

            row.append(
                f"\033[38;2;{fg[0]};{fg[1]};{fg[2]}m"
                f"\033[48;2;{bg[0]};{bg[1]};{bg[2]}m"
                f"{ch}\033[0m"
            )
        lines.append(''.join(row))

    with open(output_path, 'w') as f:
        f.write('\n'.join(lines) + '\n')

if __name__ == '__main__':
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <input.png> <output.txt>")
        sys.exit(1)
    convert(sys.argv[1], sys.argv[2])
