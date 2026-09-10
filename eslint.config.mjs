/*
 * Verificação estática da interface.
 *
 * A interface é ~40% do código do projeto e não tinha verificação nenhuma: um
 * erro de digitação em app.js passava no CI verde e só aparecia para quem
 * abrisse a página. Isto não substitui teste de fluxo, mas pega a classe de
 * erro que mais custa aqui — nome errado, variável que não existe, `var`
 * declarada duas vezes, `case` sem `break`.
 *
 * Sem transpilação e sem empacotador: os arquivos são servidos como estão,
 * então o que vale é o que o navegador entende.
 */
import js from '@eslint/js';
import globals from 'globals';

export default [
    js.configs.recommended,
    {
        files: ['src/main/resources/static/**/*.js'],
        languageOptions: {
            ecmaVersion: 2019,
            sourceType: 'script',
            globals: {
                ...globals.browser,
                // Definido por prefs.js e consumido por app.js. São dois
                // <script src> na mesma página, sem módulos entre eles.
                Prefs: 'readonly'
            }
        },
        rules: {
            eqeqeq: ['error', 'smart'],
            'no-var': 'off',
            'prefer-const': 'off',
            'no-implicit-globals': 'error',
            // caughtErrors desligado: o projeto usa `catch (e)` sem tocar em
            // `e` de propósito, quando o que importa é seguir pelo caminho
            // alternativo e não o motivo da falha. Reclamar disso seria pedir
            // uma reescrita sem ganho nenhum.
            'no-unused-vars': ['error', { args: 'after-used', caughtErrors: 'none' }],
            'no-shadow': 'error',
            'no-fallthrough': 'error',
            'no-console': ['warn', { allow: ['warn', 'error'] }]
        }
    },
    {
        // O service worker roda noutro escopo: não tem window nem document,
        // e tem caches, clients e skipWaiting.
        files: ['src/main/resources/static/sw.js'],
        languageOptions: {
            globals: globals.serviceworker
        },
        rules: {
            // No service worker o escopo de topo é o escopo global dele, e
            // não a página: `var CACHE` ali é o certo, não um vazamento.
            'no-implicit-globals': 'off'
        }
    }
];
