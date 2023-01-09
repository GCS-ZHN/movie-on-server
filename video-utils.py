#!/usr/bin/env python3
import glob
import os
import argparse
import hashlib
import shutil

FFMPEG_LOG_LEVEL = "error"


def from_hls_to_mp4(m3u8_file_path: str,
                    output_file_path=None,
                    remove_segments=True):
    """Converts HLS m3u8 file to mp4 file

    Args:
        m3u8_file_path (str): Path of HLS m3u8 file
        output_file_path (str): Path of output mp4 file
    """
    if not output_file_path:
        output_file_path = m3u8_file_path.removesuffix(".m3u8") + ".mp4"
    output_file_dir = os.path.dirname(output_file_path)
    if output_file_dir.endswith(".hls"):
        output_file_path = output_file_dir.removesuffix(".hls") + ".mp4"
    print(f"Converting {m3u8_file_path} to {output_file_path}")
    status = os.system(
        f"ffmpeg -v {FFMPEG_LOG_LEVEL} -i \"{m3u8_file_path}\" -c copy \"{output_file_path}\""
    )
    if status == 0 and remove_segments:
        if output_file_dir.endswith(".hls"):
            shutil.rmtree(output_file_dir)
            print(f"Removed {output_file_dir}")
        else:
            remove_hls_segments(m3u8_file_path)


def from_mp4_to_hls(mp4_file_path: str,
                    output_file_path=None,
                    remove_input=False,
                    key_info=None):
    """Converts mp4 file to ts segments

    Args:
        mp4_file_path (str): Path of mp4 file
        output_file_path (str): Path of output HLS m3u8 file
    """
    if not output_file_path:
        output_file_dir = mp4_file_path.removesuffix(".mp4")
        basename = os.path.basename(output_file_dir)
        output_file_dir += ".hls"
        output_file_path = os.path.join(output_file_dir, basename + ".m3u8")
    else:
        output_file_dir = os.path.dirname(output_file_path)
    os.makedirs(output_file_dir, exist_ok=True, mode=0o755)
    print(f"Calculating MD5 of {mp4_file_path}")
    with open(mp4_file_path, "rb") as f:
        md5 = hashlib.md5()
        while True:
            data = f.read(65536)
            if not data:
                break
            md5.update(data)
        md5_hex = md5.hexdigest()[:6]
    print(f"Converting {mp4_file_path} to {output_file_path}")
    output_ts_path = os.path.join(output_file_dir, f"{md5_hex}_%04d.ts")
    key_info_str = "" if not key_info else f"-hls_key_info_file {key_info}"
    status = os.system(
        f"ffmpeg -v {FFMPEG_LOG_LEVEL} -i \"{mp4_file_path}\" -c copy {key_info_str} -hls_time 10 -hls_playlist_type vod -hls_list_size 0 -hls_segment_filename \"{output_ts_path}\" \"{output_file_path}\" "
    )
    if status == 0 and remove_input:
        os.remove(mp4_file_path)
        print(f"Removed {mp4_file_path}")


def remove_hls_segments(m3u8_file_path: str):
    """Removes HLS segments

    Args:
        m3u8_file_path (str): Path of HLS m3u8 file
    """
    output_file_dir = os.path.dirname(m3u8_file_path)
    with open(m3u8_file_path, "r") as f:
        for line in f:
            line = line.rstrip()
            if line.endswith(".ts"):
                os.remove(os.path.join(output_file_dir, line))
    os.remove(m3u8_file_path)
    print(f"Removed HLS {m3u8_file_path} and segments")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="Input file path")
    parser.add_argument("--output",
                        "-o",
                        help="Output file path, Only valid for file input")
    parser.add_argument(
        "--glob-type",
        "-t",
        default="mp4",
        help=
        "Glob input type (mp4 or m3u8), Only valid for directory input. Default: mp4"
    )
    parser.add_argument("--remove-input",
                        "-r",
                        action="store_true",
                        help="Remove input file")
    parser.add_argument(
        "--recursive",
        "-R",
        action="store_true",
        help="Recursive input directory, Only valid for directory input")
    parser.add_argument(
        "--key-info",
        "-k",
        help=
        "Key info file path for HLS encrpted, Only valid for mp4 to hls conversion, Default: None"
    )
    args = parser.parse_args()
    if os.path.isfile(args.input):
        if args.input.endswith(".m3u8"):
            from_hls_to_mp4(args.input, args.output, args.remove_input)
        elif args.input.endswith(".mp4"):
            from_mp4_to_hls(args.input, args.output, args.remove_input,
                            args.key_info)
        else:
            print("Invalid input file type")
    elif os.path.isdir(args.input):
        assert args.glob_type in ["mp4", "m3u8"], "Invalid glob type"
        if args.output:
            print("Output file path is ignored for directory input")
        for file_path in glob.glob(os.path.join(args.input, "**"),
                                   recursive=args.recursive):
            if not file_path.endswith(args.glob_type):
                continue
            if args.glob_type == "mp4":
                from_mp4_to_hls(file_path, None, args.remove_input, args.key_info)
            elif args.glob_type == "m3u8":
                from_hls_to_mp4(file_path, None, args.remove_input)
    else:
        print("Invalid input path")
