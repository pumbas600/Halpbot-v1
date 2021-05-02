from sympy import sympify, preview, Function, diff, Add
from sympy import integrate as inte
from sympy.abc import *
from io import BytesIO
import base64

x, y, z, t, a, b, c = symbols('x y z t a b c')
k, m, n = symbols('k m n', integer=True)
f, g, h = symbols('f g h', cls=Function)

def display(expr: str) -> None:
    func = sympify(expr)
    generate_image(func)

def differentiate(expr: str, variable: str) -> None:
    func = sympify(expr)
    var = sympify(variable)
    derivative = diff(func, var)
    generate_image(derivative)

def integrate(expr: str, variable: str) -> None:
    func = sympify(expr)
    var = sympify(variable)
    integral = inte(func, var, manual=True)
    generate_image(integral)

def generate_image(func) -> None:
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
        b = BytesIO()
        preview(func, output='png', viewer='BytesIO', outputbuffer=b, preamble=preamble, dvioptions=['-D', '200'])

        print(200)
        print(base64.b64encode(b.getvalue()))
        return 200
    except Exception as e:
        print(400)


Method_Mappings = {
    'display': display,
    'differentiate': differentiate,
    'integrate': integrate
}

def handle_commands():
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
            try:
                if len(command) > 1:
                    method(*command[1:])
                else:
                    method()
            except Exception as e:
                print(400)
        else:
            print(404)


# if __name__ == '__main__':
#
#     f = 'x+1'
#     integrate(f, 'x')
    # print(bytes('Testing 101', 'utf-8'))
    # print(base64.b64encode(bytes('Testing 101', 'utf-8')))

handle_commands()