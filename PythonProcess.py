from sympy import sympify, preview, Function
from sympy.abc import *

x, y, z, t = symbols('x y z t')
k, m, n = symbols('k m n', integer=True)
f, g, h = symbols('f g h', cls=Function)

def display(expr: str, filename: str) -> int:
    preamble = """
    \\documentclass[varwidth,12pt]{standalone}
    \\usepackage{amsmath,amsfonts,xcolor}
    \\makeatletter
    \\newcommand{\\globalcolor}[1]{%
    \\color{#1}\\global\\let\\default@color\\current@color}
    \\makeatother
    \\definecolor{background}{RGB}{54, 57, 63}
    \\AtBeginDocument{\\globalcolor{white}}
    \\pagecolor{background}
    \\begin{document}
    """
    try:
        func = sympify(expr)
        preview(func, output='png', viewer='file', filename=filename, preamble=preamble, dvioptions=['-D', '200'])
        return 200
    except Exception:
        return 400


Method_Mappings = {
    'display': display
}


#if __name__ == '__main__':

    # f = '(-b + sqrt(b**2 - 4 * a * c))/(2 * a)'
    # print(display(f, 'demotex.png'))
    # command = 'display, (-b + sqrt(b**2 - 4 * a * c))/(2 * a), demotex.png'.split(', ')

while True:
    request = input()
    if request == 'quit':
        break

    command = request.split(', ')
    if len(command) == 0:
        print(400)
        continue

    key = command[0]
    if key in Method_Mappings:
        method = Method_Mappings[key]
        if len(command) > 1:
            print(method(*command[1:]))
        else:
            print(method())
    else:
        print(404)

